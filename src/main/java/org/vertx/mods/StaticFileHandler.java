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

package org.vertx.mods;

import org.vertx.java.core.Handler;
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

  private String webRootPrefix;
  private String indexPage;
  private boolean gzipFiles;
  private FileSystem fileSystem;

  public StaticFileHandler(String webRootPrefix, String indexPage,
      boolean gzipFiles, FileSystem fileSystem) {
    super();
    this.webRootPrefix = webRootPrefix;
    this.indexPage = indexPage;
    this.gzipFiles = gzipFiles;
    this.fileSystem = fileSystem;
  }

  public void handle(HttpServerRequest req) {
    // browser gzip capability check
    String acceptEncoding = req.headers().get("accept-encoding");
    boolean acceptEncodingGzip = acceptEncoding == null ? false : acceptEncoding.contains("gzip");
    String fileName = webRootPrefix + req.path();
    try {
      if (req.path().equals("/")) {
        req.response().sendFile(indexPage);
      } else if (!req.path().contains("..")) {
        // try to send *.gz file
        if (gzipFiles && acceptEncodingGzip) {
          boolean exists = fileSystem.existsSync(fileName + ".gz");
          if (exists) {
            // found file with gz extension
            req.response().putHeader("content-encoding", "gzip");
            req.response().sendFile(fileName + ".gz");
          } else {
            // not found gz file, try to send uncompressed file
            req.response().sendFile(fileName);
          }
        } else {
          // send not gzip file
          req.response().sendFile(fileName);
        }
      } else {
        req.response().setStatusCode(404);
        req.response().end();
      }
    } catch (Exception e) {
      throw new IllegalStateException("Failed to check file");
    }
  }

}
