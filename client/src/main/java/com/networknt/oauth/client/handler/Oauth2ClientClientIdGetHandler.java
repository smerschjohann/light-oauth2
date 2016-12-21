package com.networknt.oauth.client.handler;

import com.networknt.config.Config;
import com.networknt.oauth.client.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
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
    static String CLIENT_NOT_FOUND = "ERR12014";

    static Logger logger = LoggerFactory.getLogger(Oauth2ClientClientIdGetHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sql = "SELECT * FROM clients WHERE client_id = ?";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> result = null;
        String clientId = exchange.getQueryParameters().get("clientId").getFirst();
        result = (Map<String, Object>)PathHandlerProvider.clients.get(clientId);
        if(result != null) {
            exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
            exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
            return;
        } else {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, clientId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        result = new HashMap<>();
                        result.put("clientId", clientId);
                        result.put("clientSecret", rs.getString("client_secret"));
                        result.put("clientType", rs.getString("client_type"));
                        result.put("clientName", rs.getString("client_name"));
                        result.put("clientDesc", rs.getString("client_desc"));
                        result.put("scope", rs.getString("scope"));
                        result.put("redirectUrl", rs.getString("redirect_url"));
                        result.put("ownerId", rs.getString("owner_id"));
                        result.put("createDt", rs.getDate("create_dt"));
                        result.put("updateDt", rs.getDate("update_dt"));
                        PathHandlerProvider.clients.put(clientId, result);
                        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
                        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
                        return;
                    } else {
                        Status status = new Status(CLIENT_NOT_FOUND, clientId);
                        exchange.setStatusCode(status.getStatusCode());
                        exchange.getResponseSender().send(status.toString());
                        return;
                    }
                }
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw e;
            }
        }
    }
}
