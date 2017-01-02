package com.networknt.oauth.service.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Oauth2ServicePutHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ServicePostHandler.class);
    static final String SERVICE_NOT_FOUND = "ERR12015";
    static final String USER_NOT_FOUND = "ERR12013";

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> service = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String serviceId = (String)service.get("serviceId");

        IMap<String, Object> services = CacheStartupHookProvider.hz.getMap("services");
        if(services.get(serviceId) == null) {
            Status status = new Status(SERVICE_NOT_FOUND, serviceId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        } else {
            // make sure the owner_id exists in users map.
            String ownerId = (String)service.get("ownerId");
            if(ownerId != null) {
                IMap<String, Object> users = CacheStartupHookProvider.hz.getMap("users");
                if(!users.containsKey(ownerId)) {
                    Status status = new Status(USER_NOT_FOUND, ownerId);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                }
            }
            services.set(serviceId, service);
        }
    }
}
