package org.vertx.mods.test.integration.java;

import org.junit.Test;
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
    conf.putString("web_root", "src/test/resources").putString("host", "localhost").putNumber("port", 8181);
    container.deployModule(System.getProperty("vertx.modulename"), conf, new Handler<String>() {
      @Override
      public void handle(String deploymentID) {
        assertNotNull("deploymentID should not be null", deploymentID);
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


}
