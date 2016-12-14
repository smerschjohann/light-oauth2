package com.networknt.oauth.service.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;

public class Oauth2ServicePutHandler implements HttpHandler {

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> examples = new HashMap<String, Object>();
        examples.put("application/json", StringEscapeUtils.unescapeHtml4("{  &quot;ownerName&quot; : &quot;aeiou&quot;,  &quot;scopes&quot; : &quot;aeiou&quot;,  &quot;serviceId&quot; : &quot;aeiou&quot;,  &quot;serviceName&quot; : &quot;aeiou&quot;,  &quot;ownerEmail&quot; : &quot;aeiou&quot;}"));
        if(examples.size() > 0) {
            exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
            exchange.getResponseSender().send((String)examples.get("application/json"));
        } else {
            exchange.endExchange();
        }
    }
}
