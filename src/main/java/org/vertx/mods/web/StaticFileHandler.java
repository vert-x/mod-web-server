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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.file.FileProps;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.mods.web.mapping.UrlMapper;

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
  private boolean gzipFiles;
  private boolean caching;
  private UrlMapper urlMapper;

  private ConcurrentMap<String, Long> filePropsModified;

  private SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

  public StaticFileHandler(Vertx vertx, String webRootPrefix, String indexPage, boolean gzipFiles, boolean caching, JsonObject urlMappings) {
    super();
    this.filePropsModified = vertx.sharedData().getMap("webserver.fileProps.modified");
    this.fileSystem = vertx.fileSystem();
    this.webRootPrefix = webRootPrefix;
    this.gzipFiles = gzipFiles;
    this.caching = caching;
    urlMappings.putString("/", indexPage);
    this.urlMapper = new UrlMapper(urlMappings, webRootPrefix);
  }

  public void handle(HttpServerRequest req) {
    // browser gzip capability check
    String acceptEncoding = req.headers().get(Headers.ACCEPT_ENCODING);
    boolean acceptEncodingGzip = acceptEncoding == null ? false : acceptEncoding.contains("gzip");

    try {
      // index file may also be zipped
      String fileName = urlMapper.getFilePath(req.path());
      boolean zipped = (gzipFiles && acceptEncodingGzip);
      if (zipped && fileSystem.existsSync(fileName + ".gz")) {
        fileName += ".gz";
      }

      int error = 200;

      if (fileName.contains("..")) {
        // Prevent accessing files outside webroot
        error = 403;
      } else if (caching) {

        Date lastModifiedTime = checkCacheOrFileSystem(fileName);

        // TODO MD5 or something for etag?
        String etag = String.format("W/%d", lastModifiedTime.getTime());

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
            Date ifModifiedSinceTime = parseDateHeader(ifModifiedSince);
            if (lastModifiedTime.compareTo(ifModifiedSinceTime) >= 0) {
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
            Date ifUnmodifiedSinceTime = parseDateHeader(ifUnmodifiedSince);

            if (lastModifiedTime.after(ifUnmodifiedSinceTime)) {
              error = 412;
            }
          }
          catch (ParseException e) {
            // if date header is invalid, ignore
          }
        }

        setResponseHeader(req, Headers.ETAG, etag);
        setResponseHeader(req, Headers.LAST_MODIFIED, format.format(lastModifiedTime));
      }

      if (zipped) setResponseHeader(req, Headers.CONTENT_ENCODING, "gzip");
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

  private Date checkCacheOrFileSystem(String fileName) {

    if (filePropsModified.containsKey(fileName)) {
      return new Date(filePropsModified.get(fileName));
    }

    FileProps fileProps = fileSystem.propsSync(fileName);
    Calendar lastModifiedTime = Calendar.getInstance();
    lastModifiedTime.setTime(fileProps.lastModifiedTime());
    lastModifiedTime.set(Calendar.MILLISECOND, 0);
    filePropsModified.put(fileName, lastModifiedTime.getTime().getTime());
    return lastModifiedTime.getTime();
  }

  private Date parseDateHeader(String dateStr) throws ParseException {
    return format.parse(dateStr);
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
