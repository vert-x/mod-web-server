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

import org.vertx.java.core.http.RouteMatcher;

/**
 * A simple web server module that implements a provides
 * a default RouteMatcher that simply serves static files.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author pidster
 */
public class WebServer extends WebServerBase {

  @Override
  protected RouteMatcher routeMatcher() {
    RouteMatcher matcher = new RouteMatcher();
    matcher.noMatch(staticHandler());
    return matcher;
  }
}
