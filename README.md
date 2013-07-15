# Web Server

This is a simple example web server which efficiently serves files from the file system.

It can also be configured to act as an event bus bridge - bridging the server side event bus to client side JavaScript.

## Name

The module name is `web-server`.

## Configuration

The web-server configuration is as follows:

    {
        "web_root": <web_root>,
        "index_page": <index_page>,
        "host": <host>,
        "port": <port>,
        "static_files": <static_files>,
        "route_matcher": <route_matcher>,
        "gzip_files": <gzip_files>,

        "ssl": <ssl>,
        "key_store_password": <key_store_password>,
        "key_store_path": <key_store_path>,

        "bridge": <bridge>,
        "inbound_permitted": <inbound_permitted>,
        "outbound_permitted": <outbound_permitted>,
        "sjs_config": <sjs_config>,
        "auth_timeout": <auth_timeout>,
        "auth_address": <auth_address>
    }

* `web_root`. This is the root directory from where files will be served. *Anything that you place here or in sub directories will be externally accessible*. Default is `web`.
* `index_page`. The page to serve when the root `/` is requested. Default is `index.html`.
* `host`. The host or ip address to listen at for connections. `0.0.0.0` means listen at all available addresses. Default is `0.0.0.0`.
* `port`. The port to listen at for connections. Default is `80`.
* `static_files`. Should the server serve static files? Default is `true`.
* `route_matcher`. Should the server execute your defined RouteMatcher? Default is `false`.
* `gzip_files`. Should the server serve pre-compressed files? `true`: check file "fileName.gz", and send it back, if not found, then fallback to "fileName". `false`: send back "fileName", do not check "fileName.gz" unnecessarily. Default is `false`.
* `ssl`. Should the server use `https` as a protocol? Default is `false`.
* `key_store_password`. Password of Java keystore which holds the server certificate. Only used if `ssl` is `true`. Default is `wibble`.
* `key_store_path`. Path to keystore which holds the server certificate. Default is `server-keystore.jks`. Only used if `ssl` is `true`. *Don't put the keystore under your webroot!*.
* `bridge`. Should the server also act as an event bus bridge. This is used when you want to bridge the event bus into client side JavaScript. Default is `false`.
* `inbound_permitted`. This is an array of JSON objects representing the inbound permitted matches on the bridge. Only used if `bridge` is `true`. See the core manual for a full description of what these are. Defaults to `[]`.
* `outbound_permitted`. This is an array of JSON objects representing the outbound permitted matches on the bridge. Only used if `bridge` is `true`. See the core manual for a full description of what these are. Defaults to `[]`.
* `sjs_config`. This is a JSON object representing the configuration of the SockJS bridging application. You'd normally use this for specifying the url at which SockJS will connect to bridge from client side JS to the server. Only used if `bridge` is `true`. Default to `{"prefix": "/eventbus"}`.
* `auth_timeout`. The bridge can also cache authorisations. This determines how long the bridge will cache one for. Default value is five minutes.
* `auth_address`. The bridge can also call an authorisation manager to do authorisation. This is the address to which it will send authorisation messages. Default value is `vertx.basicauthmanager.authorise`.


## Examples

Here are some examples:

### Simple static file web server

This serves files from the web directory

    {
        "host": mycompany.com
    {

### Simple https server

    {
        "host": mycompany.com,
        "ssl": true,
        "key_store_path": "mycert.jks",
        "key_store_password": "sausages"
    }

### Event bus bridge

Pure event bus bridge that doesn't serve static files

    {
       "host": "bridgeserver.mycompany.com",
       "static_files": false,
       "bridge": true,
       "inbound_permitted": [{"address":"myservice"}],
       "outbound_permitted": [{"address":"topic.foo"}]
    }