package com.networknt.oauth.code.handler;

import com.hazelcast.core.IMap;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.code.auth.Authentication;
import com.networknt.status.Status;
import com.networknt.utility.Util;
import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Oauth2CodePostHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2CodeGetHandler.class);
    static final String CLIENT_NOT_FOUND = "ERR12014";

    static final String DEFAULT_AUTHENTICATE_CLASS = "com.networknt.oauth.code.auth.FormAuthentication";
    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

        // parse all the parameters here as this is a redirected get request.
        Map<String, String> params = new HashMap<>();
        Map<String, Deque<String>> pnames = exchange.getQueryParameters();
        for (Map.Entry<String, Deque<String>> entry : pnames.entrySet()) {
            String pname = entry.getKey();
            Iterator<String> pvalues = entry.getValue().iterator();
            if(pvalues.hasNext()) {
                params.put(pname, pvalues.next());
            }
        }
        if(logger.isDebugEnabled()) logger.debug("params", params);
        String clientId = params.get("client_id");
        // check if the client_id is valid
        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Client client = clients.get(clientId);
        if(client == null) {
            Status status = new Status(CLIENT_NOT_FOUND, clientId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        } else {
                /*
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
                */
            final SecurityContext context = exchange.getSecurityContext();
            String userId = context.getAuthenticatedAccount().getPrincipal().getName();
            // generate auth code
            String code = Util.getUUID();
            CacheStartupHookProvider.hz.getMap("codes").set(code, userId);
            String redirectUrl = params.get("redirect_url");
            if(redirectUrl == null) {
                redirectUrl = client.getRedirectUrl();
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
