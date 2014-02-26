package org.vertx.mods.test.integration.java;

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
public class WebServerTest extends TestVerticle {

  @Test
  public void testWebServer() {
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
  public void testRouteMatcher() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", false)
        .putBoolean("route_matcher", true)
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
  public void testHigherDirectoryError() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/main/java")
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
        client.getNow("/../../test/resources/index.html", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertEquals(403, resp.statusCode());

            resp.bodyHandler(new Handler<Buffer>() {
              @Override
              public void handle(Buffer body) {
                assertFalse(body.toString().contains("Armadillos!"));
                testComplete();
              }
            });
          }
        });
      }
    });
  }

  @Test
  public void testMissingFileError() {
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
        client.getNow("missingFile.html", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertEquals(404, resp.statusCode());
            testComplete();
         }
        });
      }
    });
  }

  @Test
  public void testMissingFileWithRedirect() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", true)
        .putBoolean("route_matcher", false)
        .putBoolean("redirect_404_to_index", true)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule(System.getProperty("vertx.modulename"), conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        if (ar.failed()) ar.cause().printStackTrace();
        assertTrue(ar.succeeded());
        HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        client.getNow("missingFile.html", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertEquals(200, resp.statusCode());

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

}
