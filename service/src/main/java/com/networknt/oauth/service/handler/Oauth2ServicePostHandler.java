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

public class Oauth2ServicePostHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ServicePostHandler.class);
    static final String SERVICE_ID_EXISTS = "ERR12018";
    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> service = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String serviceId = (String)service.get("serviceId");

        IMap<String, Object> services = CacheStartupHookProvider.hz.getMap("services");
        if(services.get(serviceId) == null) {
            services.set(serviceId, service);
        } else {
            Status status = new Status(SERVICE_ID_EXISTS, serviceId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        }
    }
}
