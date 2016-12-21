package com.networknt.oauth.client.handler;

import com.networknt.body.BodyHandler;
import com.networknt.oauth.client.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
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
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sql = "UPDATE clients SET client_type=?, client_name=?, client_desc=?, scope=?, redirect_url=?, owner_id=?, update_dt=? WHERE client_id=?";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> client = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, (String)client.get("clientType"));
            stmt.setString(2, (String)client.get("clientName"));
            stmt.setString(3, (String)client.get("clientDesc"));
            stmt.setString(4, (String)client.get("scope"));
            stmt.setString(5, (String)client.get("redirectUrl"));
            stmt.setString(6, (String)client.get("ownerId"));
            stmt.setDate(7, new Date(System.currentTimeMillis()));
            stmt.setString(8, (String)client.get("clientId"));
            stmt.executeUpdate();
            PathHandlerProvider.clients.put((String)client.get("clientId"), client);

        } catch (SQLException e) {
            logger.error("Exception:", e);
            // should handle this exception and return an error message.
            throw e;
        }
    }
}
