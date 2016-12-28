package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.client.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class Oauth2ClientPutHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ClientPutHandler.class);
    static String CLIENT_NOT_FOUND = "ERR12014";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> client = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String clientId = (String)client.get("clientId");

        IMap<String, Object> clients = CacheStartupHookProvider.hz.getMap("clients");
        if(clients.get(clientId) == null) {
            Status status = new Status(CLIENT_NOT_FOUND, clientId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        } else {
            clients.set(clientId, client);
        }
    }
}
