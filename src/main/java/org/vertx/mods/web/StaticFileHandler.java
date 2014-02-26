/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.mods.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.nio.file.Paths;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.file.FileProps;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * A Handler implementation specifically for serving HTTP requests
 * from the file system.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author pidster
 */
public class StaticFileHandler implements Handler<HttpServerRequest> {

  private FileSystem fileSystem;
  private String webRootPrefix;
  private String indexPage;
  private boolean gzipFiles;
  private boolean caching;
  private boolean redirect404ToIndex;

  private ConcurrentMap<String, Long> filePropsModified;

  private SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
  
  public StaticFileHandler(Vertx vertx, String webRootPrefix) {
    this(vertx, webRootPrefix, "index.html", false, true, false);
  }

  public StaticFileHandler(Vertx vertx, String webRootPrefix, boolean gzipFiles, boolean caching) {
    this(vertx, webRootPrefix, "index.html", gzipFiles, caching, false);
  }

  public StaticFileHandler(Vertx vertx, String webRootPrefix, String indexPage, 
                           boolean gzipFiles, boolean caching, boolean redirect404ToIndex) {
    super();
    this.filePropsModified = vertx.sharedData().getMap("webserver.fileProps.modified");
    this.fileSystem = vertx.fileSystem();
    this.webRootPrefix = webRootPrefix;
    this.indexPage = indexPage;
    this.gzipFiles = gzipFiles;
    this.caching = caching;
    this.redirect404ToIndex = redirect404ToIndex;
  }

  public void handle(HttpServerRequest req) {
    // browser gzip capability check
    String acceptEncoding = req.headers().get(Headers.ACCEPT_ENCODING);
    boolean acceptEncodingGzip = acceptEncoding == null ? false : acceptEncoding.contains("gzip");

    try {
      String fileName = "";
      int error = 200;
      boolean zipped = (gzipFiles && acceptEncodingGzip);

      // Ensure no /../ in the path to avoid sending everything in the file system
      if (req.path().indexOf("..") != -1) {
        error = 403;
      }
      else {
        fileName = getAbsoluteFilename(req.path(), zipped);

        if (caching) {
          long lastModifiedTime = checkCacheOrFileSystem(fileName);

          // TODO MD5 or something for etag?
          String etag = String.format("W/%d", lastModifiedTime);
          
          if (req.headers().contains(Headers.IF_MATCH)) {
            String checkEtags = req.headers().get(Headers.IF_MATCH);
            if (checkEtags.indexOf(',') > -1) {
              // there may be multiple etags
              boolean matched = false;
              LOOP : for (String checkEtag : checkEtags.split(", *")) {
                if (etag.equals(checkEtag)) {
                  matched = true;
                  break LOOP;
                }
              }
              if (!matched) error = 412;
            }
            // wildcards are allowed
            else if ("*".equals(checkEtags) && !fileSystem.existsSync(fileName)) {
              error = 412;
            }
            else if (etag.equals(checkEtags)) {
              error = 304;
            }
          }

          // either if-none-match or if-modified-since header, then...
          else if (req.headers().contains(Headers.IF_NONE_MATCH)) {
            String checkEtags = req.headers().get(Headers.IF_NONE_MATCH);

            // only HEAD or GET are allowed
            if ("HEAD".equals(req.method()) || "GET".equals(req.method())) {
              if (checkEtags.indexOf(',') > -1) {
                // there may be multiple etags
                LOOP : for (String checkEtag : checkEtags.split(", *")) {
                  System.out.println(etag + " == " + checkEtag);

                  if (etag.equals(checkEtag)) {
                    error = 304;
                    break LOOP;
                  }
                }
              }
              // wildcards are allowed
              else if ("*".equals(checkEtags)) {
                error = 304;
              }
              else if (etag.equals(checkEtags)) {
                error = 304;
              }
            }
            else {
              sendError(req, 412);
            }
          }
          else if (req.headers().contains(Headers.IF_MODIFIED_SINCE)) {
            try {
              String ifModifiedSince = req.headers().get(Headers.IF_MODIFIED_SINCE);
              long ifModifiedSinceTime = parseDateHeader(ifModifiedSince);

              if (lastModifiedTime == ifModifiedSinceTime) {
                error = 304;
              }
            }
            catch (ParseException e) {
              // if date header is invalid, ignore
            }
          }

          if (req.headers().contains(Headers.IF_UNMODIFIED_SINCE)) {
            try {
              String ifUnmodifiedSince = req.headers().get(Headers.IF_UNMODIFIED_SINCE);
              long ifUnmodifiedSinceTime = parseDateHeader(ifUnmodifiedSince);

              if (lastModifiedTime > ifUnmodifiedSinceTime) {
                error = 412;
              }
            }
            catch (ParseException e) {
              // if date header is invalid, ignore
            }
          }

          setResponseHeader(req, Headers.ETAG, etag);
          setResponseHeader(req, Headers.LAST_MODIFIED, format.format(new Date(lastModifiedTime)));
        }

        if (zipped) setResponseHeader(req, Headers.CONTENT_ENCODING, "gzip");
      }

      if (error != 200) {
        sendError(req, error);
      }
      else {
        if ("HEAD".equals(req.method())) {
          req.response().end();
        }
        else {
          req.response().sendFile(fileName);
        }
      }

    } catch (Exception e) {
      throw new IllegalStateException("Failed to check file: " + e.getMessage());
    }
  }

  private String getAbsoluteFilename(String relativePath, boolean zipped) {
    String result = (relativePath.equals("/") ? indexPage : Paths.get(webRootPrefix, relativePath).toString());

    // index file may also be zipped
    if (zipped && fileSystem.existsSync(result + ".gz")) {
      result += ".gz";
    }
    else if ((redirect404ToIndex) && (relativePath != "/")) {
      if( !fileSystem.existsSync(result))
        result = getAbsoluteFilename("/", zipped);
    }
    return result;
  }

  private long checkCacheOrFileSystem(String fileName) {

    if (filePropsModified.containsKey(fileName)) {
      return filePropsModified.get(fileName);
    }

    FileProps fileProps = fileSystem.propsSync(fileName);

    // HTTP headers are only accurate to nearest second
    long seconds = (fileProps.lastModifiedTime().getTime() / 1000) * 1000; 

    filePropsModified.put(fileName, seconds);
    return seconds;
  }

  private long parseDateHeader(String dateStr) throws ParseException {
    Date date = format.parse(dateStr);
    return date.getTime();
  }

  private void setResponseHeader(HttpServerRequest req, String header, String value) {
    req.response().putHeader(header, value);
  }

  private void sendError(HttpServerRequest req, int error) {
    sendError(req, error, "");
  }

  private void sendError(HttpServerRequest req, int error, String message) {
    req.response().setStatusMessage(message);
    req.response().setStatusCode(error);
    req.response().end();
  }

}
