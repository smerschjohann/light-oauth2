package com.networknt.oauth.user.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Oauth2UserPutHandler implements HttpHandler {
    static final String USER_NOT_FOUND = "ERR12013";
    static Logger logger = LoggerFactory.getLogger(Oauth2UserPostHandler.class);
    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> user = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String userId = (String)user.get("userId");
        IMap<String, Object> users = CacheStartupHookProvider.hz.getMap("users");
        Map<String, Object> uMap = (Map<String, Object>)users.get(userId);
        if(uMap == null) {
            Status status = new Status(USER_NOT_FOUND, userId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        } else {
            // as password is not in the return value, chances are password is not in the user object
            user.put("password", uMap.get("password"));
            users.set(userId, user);
        }
    }
}
