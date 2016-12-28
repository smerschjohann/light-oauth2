package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.client.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import com.networknt.utility.Util;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.util.UUID;
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

public class Oauth2ClientPostHandler implements HttpHandler {

    static Logger logger = LoggerFactory.getLogger(Oauth2ClientPostHandler.class);
    static String CLIENT_ID_EXISTS = "ERR12019";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> client = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);

        // generate client_id and client_secret here.
        String clientId = UUID.randomUUID().toString();
        client.put("clientId", clientId);
        String clientSecret = Util.getUUID();
        client.put("clientSecret", clientSecret);
        client.put("createDt", new Date(System.currentTimeMillis()));

        IMap<String, Object> clients = CacheStartupHookProvider.hz.getMap("clients");
        if(clients.get(clientId) == null) {
            clients.set(clientId, client);
        } else {
            Status status = new Status(CLIENT_ID_EXISTS, clientId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
    }
}
