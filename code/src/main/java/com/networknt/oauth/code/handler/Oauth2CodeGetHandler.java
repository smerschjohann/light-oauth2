package com.networknt.oauth.code.handler;

import com.networknt.oauth.cache.CacheStartupHookProvider;
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

/**
 * This handler is for get request, the credentials might in header or just query parameters.
 * Need to check client info in order to find out which class to handle the authentication
 * clients are cached so that it has better performance. If client_id cannot be found in cache,
 * go to db to get it. It must be something added recently and not in cache yet.
 *
 */
public class Oauth2CodeGetHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2CodeGetHandler.class);
    static final String INVALID_CODE_REQUEST = "ERR12009";
    static final String CLIENT_NOT_FOUND = "ERR12014";
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
        String responseType = params.get("response_type");
        String clientId = params.get("client_id");
        if(responseType == null || clientId == null) {
            Status status = new Status(INVALID_CODE_REQUEST);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        } else {
            // check if the client_id is valid
            Map<String, Object> client = (Map<String, Object>) CacheStartupHookProvider.hz.getMap("clients").get(clientId);
            if(client == null) {
                Status status = new Status(CLIENT_NOT_FOUND, clientId);
                exchange.setStatusCode(status.getStatusCode());
                exchange.getResponseSender().send(status.toString());
            } else {
                String code = Util.getUUID();
                final SecurityContext context = exchange.getSecurityContext();
                String userId = context.getAuthenticatedAccount().getPrincipal().getName();

                CacheStartupHookProvider.hz.getMap("code").put(code, userId);
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
