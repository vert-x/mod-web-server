package io.vertx.test;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.mods.web.WebServerBase;

public class TestWebServer extends WebServerBase {

  @Override
  protected RouteMatcher routeMatcher() {
    RouteMatcher matcher = new RouteMatcher();
    matcher.get("/foo/:bar", simpleHandler("bar"));
    matcher.getWithRegEx("/bar/.+", simpleHandler("foo"));
    return matcher;
  }

  private Handler<HttpServerRequest> simpleHandler(final String response) {
    return new Handler<HttpServerRequest>() {
      @Override
      public void handle(HttpServerRequest req) {
        req.response().end(response);
      }
    };
  }

}
