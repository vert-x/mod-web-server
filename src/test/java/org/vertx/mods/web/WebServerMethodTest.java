package org.vertx.mods.web;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

/**
 *
 */
public class WebServerMethodTest extends TestVerticle {

  @Test
  public void testGet() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", true)
        .putBoolean("route_matcher", false)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule(System.getProperty("vertx.modulename"), conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        if (ar.failed()) ar.cause().printStackTrace();
        assertTrue(ar.succeeded());
        HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        client.getNow("/", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertTrue(resp.headers().contains(Headers.CONTENT_LENGTH));
            resp.bodyHandler(new Handler<Buffer>() {
              @Override
              public void handle(Buffer body) {
                assertTrue(body.toString().contains("Armadillos!"));
                testComplete();
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testHead() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", true)
        .putBoolean("route_matcher", false)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule(System.getProperty("vertx.modulename"), conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        if (ar.failed()) ar.cause().printStackTrace();
        assertTrue(ar.succeeded());
        HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        HttpClientRequest request = client.head("/", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertTrue(resp.headers().contains(Headers.CONTENT_LENGTH));
            assertEquals("0", resp.headers().get(Headers.CONTENT_LENGTH));
            testComplete();
          }
        });
        request.end();
      }
    });
  }

}
