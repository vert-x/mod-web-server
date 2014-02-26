package org.vertx.mods.test.integration.java;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

/**
 *
 */
public class WebServerHeadersTest extends TestVerticle {

  @Test
  public void testCachingHeaders() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", true)
        .putBoolean("route_matcher", false)
        .putBoolean("caching", true)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule(System.getProperty("vertx.modulename"), conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        assertTrue(ar.succeeded());
        HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        client.getNow("/", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertTrue(resp.headers().contains("etag"));
            assertTrue(resp.headers().contains("last-modified"));
            testComplete();
          }
        });
      }
    });
  }

  @Test
  public void testEtagIfNoneMatch() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", true)
        .putBoolean("route_matcher", false)
        .putBoolean("caching", true)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule(System.getProperty("vertx.modulename"), conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        assertTrue(ar.succeeded());
        final HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        client.getNow("/", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertTrue(resp.headers().contains("etag"));
            String etag = resp.headers().get("etag");
            HttpClientRequest request = client.get("/", new Handler<HttpClientResponse>() {
              @Override
              public void handle(HttpClientResponse event) {
                assertEquals(304, event.statusCode());
                testComplete();
              }
            });
            request.putHeader("If-None-Match", etag);
            request.end();
          }
        });
      }
    });
  }

  @Test
  public void testEtagIfMatchWildcard() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", true)
        .putBoolean("route_matcher", false)
        .putBoolean("caching", true)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule(System.getProperty("vertx.modulename"), conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        assertTrue(ar.succeeded());
        final HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        client.getNow("/", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertTrue(resp.headers().contains("etag"));
            HttpClientRequest request = client.get("/", new Handler<HttpClientResponse>() {
              @Override
              public void handle(HttpClientResponse event) {
                assertEquals(200, event.statusCode());
                testComplete();
              }
            });
            request.putHeader("If-Match", "*");
            request.end();
          }
        });
      }
    });
  }

  @Test
  public void testEtagIfMatch() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", true)
        .putBoolean("route_matcher", false)
        .putBoolean("caching", true)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule(System.getProperty("vertx.modulename"), conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        assertTrue(ar.succeeded());
        final HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        client.getNow("/", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertTrue(resp.headers().contains("etag"));
            String etag = resp.headers().get("etag");
            HttpClientRequest request = client.get("/", new Handler<HttpClientResponse>() {
              @Override
              public void handle(HttpClientResponse event) {
                assertEquals(304, event.statusCode());
                testComplete();
              }
            });
            request.putHeader("If-Match", etag);
            request.end();
          }
        });
      }
    });
  }

  @Test
  public void testEtagIfNoneMatchWildcard() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", true)
        .putBoolean("route_matcher", false)
        .putBoolean("caching", true)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule(System.getProperty("vertx.modulename"), conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        assertTrue(ar.succeeded());
        final HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        client.getNow("/", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertTrue(resp.headers().contains("etag"));

            HttpClientRequest request = client.get("/", new Handler<HttpClientResponse>() {
              @Override
              public void handle(HttpClientResponse event) {
                assertEquals(304, event.statusCode());
                testComplete();
              }
            });
            request.putHeader("If-None-Match", "*");
            request.end();
          }
        });
      }
    });
  }

  @Test
  public void testEtagIfModifiedSince() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", true)
        .putBoolean("route_matcher", false)
        .putBoolean("caching", true)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule(System.getProperty("vertx.modulename"), conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        assertTrue(ar.succeeded());
        final HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        client.getNow("/", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertTrue(resp.headers().contains("last-modified"));

            String lastModifiedStr = resp.headers().get("last-modified");
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
            try {
              Date date = format.parse(lastModifiedStr);
              HttpClientRequest request = client.get("/", new Handler<HttpClientResponse>() {
                @Override
                public void handle(HttpClientResponse event) {
                  assertEquals(304, event.statusCode());
                  testComplete();
                }
              });
              request.putHeader("If-Modified-Since", format.format(date));
              request.end();

            } catch (ParseException e) {
              fail(e.getMessage());
            }
          }
        });
      }
    });
  }

  @Test
  public void testEtagIfUnModifiedSince() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", true)
        .putBoolean("route_matcher", false)
        .putBoolean("caching", true)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule(System.getProperty("vertx.modulename"), conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        assertTrue(ar.succeeded());
        final HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        client.getNow("/", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertTrue(resp.headers().contains("last-modified"));

            String lastModifiedStr = resp.headers().get("last-modified");
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
            try {
              Date date1 = format.parse(lastModifiedStr);
              HttpClientRequest request = client.get("/", new Handler<HttpClientResponse>() {
                @Override
                public void handle(HttpClientResponse event) {
                  assertEquals(200, event.statusCode());
                  testComplete();
                }
              });
              request.putHeader("If-Unmodified-Since", format.format(date1));
              request.end();

            } catch (ParseException e) {
              fail(e.getMessage());
            }
          }
        });
      }
    });
  }

  @Test
  public void testEtagIfUnModifiedSinceError() {
    JsonObject conf = new JsonObject();
    conf.putString("web_root", "src/test/resources")
        .putBoolean("static_files", true)
        .putBoolean("route_matcher", false)
        .putBoolean("caching", true)
        .putString("host", "localhost")
        .putNumber("port", 8181);
    container.deployModule(System.getProperty("vertx.modulename"), conf, new AsyncResultHandler<String>() {
      @Override
      public void handle(AsyncResult<String> ar) {
        assertTrue(ar.succeeded());
        final HttpClient client = vertx.createHttpClient();
        client.setHost("localhost").setPort(8181);
        client.getNow("/", new Handler<HttpClientResponse>() {
          @Override
          public void handle(HttpClientResponse resp) {
            assertTrue(resp.headers().contains("last-modified"));

            String lastModifiedStr = resp.headers().get("last-modified");
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
            try {
              Date date1 = format.parse(lastModifiedStr);
              Date date2 = new Date(date1.getTime() - 5000);
              HttpClientRequest request = client.get("/", new Handler<HttpClientResponse>() {
                @Override
                public void handle(HttpClientResponse event) {
                  assertEquals(412, event.statusCode());
                  testComplete();
                }
              });
              request.putHeader("If-Unmodified-Since", format.format(date2));
              request.end();

            } catch (ParseException e) {
              fail(e.getMessage());
            }
          }
        });
      }
    });
  }


}
