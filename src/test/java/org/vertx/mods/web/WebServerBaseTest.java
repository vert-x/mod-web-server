package org.vertx.mods.web;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

/**
 *
 */
public class WebServerBaseTest extends TestVerticle {

  @Test
  public void testWebServerBaseExtensionRegex() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", false)
        .putBoolean("route_matcher", true)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule("io.vertx~webtest~1.0.0", conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        if (ar.failed()) ar.cause().printStackTrace();
        assertTrue(ar.succeeded());

        HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        client.getNow("/bar/foo", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            resp.bodyHandler(new Handler<Buffer>() {
              @Override
              public void handle(Buffer body) {
                assertTrue(body.toString().contains("foo"));
                testComplete();
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testWebServerBaseExtension() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", false)
        .putBoolean("route_matcher", true)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule("io.vertx~webtest~1.0.0", conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        if (ar.failed()) ar.cause().printStackTrace();
        assertTrue(ar.succeeded());

        HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        client.getNow("/foo/bar", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            resp.bodyHandler(new Handler<Buffer>() {
              @Override
              public void handle(Buffer body) {
                assertTrue(body.toString().contains("bar"));
                testComplete();
              }
            });
          }
        });
      }
    });
  }

}
