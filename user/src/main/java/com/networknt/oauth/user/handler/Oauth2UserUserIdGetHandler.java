package com.networknt.oauth.user.handler;

import com.networknt.config.Config;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Oauth2UserUserIdGetHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2UserUserIdGetHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> result = new HashMap<>();

        String userId = exchange.getQueryParameters().get("userId").getFirst();
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result.put("userId", userId);
                    result.put("userType", rs.getString("user_type"));
                    result.put("firstName", rs.getString("first_name"));
                    result.put("lastName", rs.getString("last_name"));
                    result.put("email", rs.getString("email"));
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
