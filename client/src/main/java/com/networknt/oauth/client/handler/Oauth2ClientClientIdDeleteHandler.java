package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.client.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class Oauth2ClientClientIdDeleteHandler implements HttpHandler {
    static String CLIENT_NOT_FOUND = "ERR12014";

    static Logger logger = LoggerFactory.getLogger(Oauth2ClientClientIdDeleteHandler.class);
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String clientId = exchange.getQueryParameters().get("clientId").getFirst();

        IMap<String, Object> clients = CacheStartupHookProvider.hz.getMap("clients");
        if(clients.get(clientId) == null) {
            Status status = new Status(CLIENT_NOT_FOUND, clientId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        } else {
            clients.delete(clientId);
        }
    }
}
