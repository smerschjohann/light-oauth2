package com.networknt.oauth.code.handler;

import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.code.auth.Authentication;
import com.networknt.status.Status;
import com.networknt.utility.Util;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Oauth2CodePostHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2CodeGetHandler.class);
    static final String INVALID_CODE_REQUEST = "ERR12009";
    static final String CLIENT_NOT_FOUND = "ERR12014";
    static final String MISSING_AUTHORIZATION_HEADER = "ERR12002";

    static final String DEFAULT_AUTHENTICATE_CLASS = "com.networknt.oauth.code.auth.FormAuthentication";
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

        // parse all the parameters here as this is a redirected get request.
        Map<String, String> params = new HashMap<String, String>();
        Map<String, Deque<String>> pnames = exchange.getQueryParameters();
        for (Map.Entry<String, Deque<String>> entry : pnames.entrySet()) {
            String pname = entry.getKey();
            Iterator<String> pvalues = entry.getValue().iterator();
            if(pvalues.hasNext()) {
                params.put(pname, pvalues.next());
            }
        }
        if(logger.isDebugEnabled()) logger.debug("params", params);
        String responseType = params.get("response_type");
        String clientId = params.get("client_id");
        if(responseType == null || clientId == null) {
            Status status = new Status(INVALID_CODE_REQUEST);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        } else {
            // check if the client_id is valid
            Map<String, Object> client = (Map<String, Object>) CacheStartupHookProvider.hz.getMap("clients").get(clientId);
            if(client == null) {
                Status status = new Status(CLIENT_NOT_FOUND, clientId);
                exchange.setStatusCode(status.getStatusCode());
                exchange.getResponseSender().send(status.toString());
                return;
            } else {
                String clazz = (String)client.get("authenticateClass");
                if(clazz == null) clazz = DEFAULT_AUTHENTICATE_CLASS;
                Authentication auth = (Authentication)Class.forName(clazz).getConstructor().newInstance();
                String userId = auth.authenticate(exchange);
                if(userId == null) {
                    Status status = new Status(MISSING_AUTHORIZATION_HEADER, clientId);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                    return;
                }
                if(logger.isDebugEnabled()) logger.debug("User is authenticated " + userId);
                // generate auth code
                String code = Util.getUUID();
                CacheStartupHookProvider.hz.getMap("codes").put(code, userId);
                String redirectUrl = params.get("redirect_url");
                if(redirectUrl == null) {
                    redirectUrl = (String)client.get("redirectUrl");
                }
                redirectUrl = redirectUrl + "?code=" + code;
                if(logger.isDebugEnabled()) logger.debug("redirectUrl = " + redirectUrl);
                // now redirect here.
                exchange.setStatusCode(StatusCodes.FOUND);
                exchange.getResponseHeaders().put(Headers.LOCATION, redirectUrl);
                exchange.endExchange();
            }
        }
    }
}
