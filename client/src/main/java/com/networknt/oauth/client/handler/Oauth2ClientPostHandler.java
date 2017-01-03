package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.User;
import com.networknt.status.Status;
import com.networknt.utility.Util;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.Map;
import java.util.UUID;

public class Oauth2ClientPostHandler implements HttpHandler {

    static Logger logger = LoggerFactory.getLogger(Oauth2ClientPostHandler.class);
    static final String CLIENT_ID_EXISTS = "ERR12019";
    static final String USER_NOT_FOUND = "ERR12013";

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> client = (Map<String, Object>)exchange.getAttachment(BodyHandler.REQUEST_BODY);

        // generate client_id and client_secret here.
        String clientId = UUID.randomUUID().toString();
        client.put("clientId", clientId);
        String clientSecret = Util.getUUID();
        client.put("clientSecret", clientSecret);
        client.put("createDt", new Date(System.currentTimeMillis()));

        IMap<String, Object> clients = CacheStartupHookProvider.hz.getMap("clients");
        if(clients.get(clientId) == null) {
            // make sure the owner_id exists in users map.
            String ownerId = (String)client.get("ownerId");
            if(ownerId != null) {
                IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
                if(!users.containsKey(ownerId)) {
                    Status status = new Status(USER_NOT_FOUND, ownerId);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                }
            }
            clients.set(clientId, client);
            // send the client back with client_id and client_secret
            exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(client));
        } else {
            Status status = new Status(CLIENT_ID_EXISTS, clientId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        }
    }
}
