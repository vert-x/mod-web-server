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

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;

import java.io.File;

/**
 * A simple web server base module that can serve static files, provides an
 * extension point for a configurable RouteMatcher, and also can bridge 
 * event bus messages to/from client side JavaScript and the server side
 * event bus.
 *
 * Please see the modules manual for full description of what configuration
 * parameters it takes.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author pidster
 */
public abstract class WebServerBase extends BusModBase {

  public static final int DEFAULT_PORT = 80;

  public static final String DEFAULT_ADDRESS = "0.0.0.0";

  public static final String DEFAULT_WEB_ROOT = "web";

  public static final String DEFAULT_INDEX_PAGE = "index.html";

  public static final String DEFAULT_AUTH_ADDRESS = "vertx.basicauthmanager.authorise";

  public static final long DEFAULT_AUTH_TIMEOUT = 5 * 60 * 1000;

  @Override
  public void start(final Future<Void> result) {
    start();

    HttpServer server = vertx.createHttpServer();

    if (getOptionalBooleanConfig("ssl", false)) {
      server.setSSL(true).setKeyStorePassword(getOptionalStringConfig("key_store_password", "wibble"))
                         .setKeyStorePath(getOptionalStringConfig("key_store_path", "server-keystore.jks"));
        if (getOptionalBooleanConfig("client_auth_required", false)) {
        server.setClientAuthRequired(true).setTrustStorePassword(getOptionalStringConfig("trust_store_password", "wibble"))
                                          .setTrustStorePath(getOptionalStringConfig("trust_store_path", "server-truststore.jks"));
        }
    }

    if (getOptionalBooleanConfig("route_matcher", false)) {
      server.requestHandler(routeMatcher());
    }
    else if (getOptionalBooleanConfig("static_files", true)) {
      server.requestHandler(staticHandler());
    }

    // Must always bridge AFTER setting request handlers
    boolean bridge = getOptionalBooleanConfig("bridge", false);
    if (bridge) {
      SockJSServer sjsServer = vertx.createSockJSServer(server);
      JsonArray inboundPermitted = getOptionalArrayConfig("inbound_permitted", new JsonArray());
      JsonArray outboundPermitted = getOptionalArrayConfig("outbound_permitted", new JsonArray());

      sjsServer.bridge(getOptionalObjectConfig("sjs_config", new JsonObject().putString("prefix", "/eventbus")),
          inboundPermitted, outboundPermitted,
          getOptionalLongConfig("auth_timeout", DEFAULT_AUTH_TIMEOUT),
          getOptionalStringConfig("auth_address", DEFAULT_AUTH_ADDRESS));
    }

    server.listen(getOptionalIntConfig("port", DEFAULT_PORT), getOptionalStringConfig("host", DEFAULT_ADDRESS), new AsyncResultHandler<HttpServer>() {
      @Override
      public void handle(AsyncResult<HttpServer> ar) {
        if (!ar.succeeded()) {
          result.setFailure(ar.cause());
        } else {
          result.setResult(null);
        }
      }
    });
  }

  /**
   * @return RouteMatcher
   */
  protected abstract RouteMatcher routeMatcher();

  /**
   * @return Handler for serving static files
   */
  protected Handler<HttpServerRequest> staticHandler() {
    String webRoot = getOptionalStringConfig("web_root", DEFAULT_WEB_ROOT);
    String indexPage = getOptionalStringConfig("index_page", DEFAULT_INDEX_PAGE);
    String webRootPrefix = webRoot + File.separator;
    JsonObject urlMappings = getOptionalObjectConfig("urlMappings", new JsonObject());
    boolean gzipFiles = getOptionalBooleanConfig("gzip_files", false);
    boolean caching = getOptionalBooleanConfig("caching", false);

    return new StaticFileHandler(vertx, webRootPrefix, indexPage, gzipFiles, caching, urlMappings);
  }

}
