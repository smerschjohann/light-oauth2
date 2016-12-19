package com.networknt.oauth.client.handler;

import com.networknt.config.Config;
import com.networknt.oauth.client.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class Oauth2ClientClientIdGetHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ClientClientIdGetHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sql = "SELECT * FROM clients WHERE client_id = ?";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> result = new HashMap<>();

        String clientId = exchange.getQueryParameters().get("clientId").getFirst();
        result = (Map<String, Object>)PathHandlerProvider.clients.get(clientId);
        if(result != null) {
            exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
            exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
        } else {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, clientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        result.put("clientId", clientId);
                        result.put("clientSecret", rs.getString("client_secret"));
                        result.put("clientType", rs.getString("client_type"));
                        result.put("clientName", rs.getString("client_name"));
                        result.put("clientDesc", rs.getString("client_desc"));
                        result.put("scope", rs.getString("scope"));
                        result.put("redirectUrl", rs.getString("redirect_url"));
                        result.put("ownerId", rs.getString("owner_id"));
                        result.put("ownerName", rs.getString("owner_name"));
                        result.put("ownerEmail", rs.getString("owner_email"));
                        result.put("createDt", rs.getDate("create_dt"));
                        result.put("updateDt", rs.getDate("update_dt"));
                        PathHandlerProvider.clients.put(clientId, result);
                        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
                        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
                    } else {
                        // TODO not found.
                    }
                }
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw e;
            }
        }
    }
}
