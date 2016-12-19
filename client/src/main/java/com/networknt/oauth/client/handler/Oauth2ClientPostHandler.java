package com.networknt.oauth.client.handler;

import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.oauth.client.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
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
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sql = "INSERT INTO clients (client_id, client_secret, client_type, client_name, client_desc, scope, " +
            "redirect_url, owner_id, owner_name, owner_email, create_dt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> client = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        // generate client_id and client_secret here.
        String clientId = UUID.randomUUID().toString();
        client.put("clientId", clientId);
        String clientSecret = Util.getUUID();
        client.put("clientSecret", clientSecret);
        client.put("createDt", new Date(System.currentTimeMillis()));
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, clientId);
            stmt.setString(2, clientSecret);
            stmt.setString(3, (String)client.get("clientType"));
            stmt.setString(4, (String)client.get("clientName"));
            stmt.setString(5, (String)client.get("clientDesc"));
            stmt.setString(6, (String)client.get("scope"));
            stmt.setString(7, (String)client.get("redirectUrl"));
            stmt.setString(8, (String)client.get("ownerId"));
            stmt.setString(9, (String)client.get("ownerName"));
            stmt.setString(10, (String)client.get("ownerEmail"));
            stmt.setDate(11, (Date)client.get("createDt"));
            stmt.executeUpdate();
            // put it into the cache
            PathHandlerProvider.clients.put(clientId, client);
            exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
            exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(client));
        } catch (SQLException e) {
            logger.error("Exception:", e);
            // should handle this exception and return an error message.
            throw e;
        }
    }
}
