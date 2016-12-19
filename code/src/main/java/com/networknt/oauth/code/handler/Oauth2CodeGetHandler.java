package com.networknt.oauth.code.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * This handler is for get request, the credentials might in header or just query parameters.
 * Need to check client info in order to find out which class to handle the authentication
 * clients are cached so that it has better performance. If client_id cannot be found in cache,
 * go to db to get it. It must be something added recently and not in cache yet.
 *
 */
public class Oauth2CodeGetHandler implements HttpHandler {

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> examples = new HashMap<String, Object>();
        if(examples.size() > 0) {
            exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
            exchange.getResponseSender().send((String)examples.get("application/json"));
        } else {
            exchange.endExchange();
        }
    }
}
