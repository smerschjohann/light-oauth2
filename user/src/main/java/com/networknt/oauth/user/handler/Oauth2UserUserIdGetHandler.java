package com.networknt.oauth.user.handler;

import com.networknt.config.Config;
import com.networknt.oauth.user.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.HttpString;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.server.UnicastServerRef;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Oauth2UserUserIdGetHandler implements HttpHandler {
    static String USER_NOT_FOUND = "ERR12013";

    static Logger logger = LoggerFactory.getLogger(Oauth2UserUserIdGetHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> result = null;
        String userId = exchange.getQueryParameters().get("userId").getFirst();
        result = (Map<String, Object>)PathHandlerProvider.users.get(userId);
        if(result != null) {
            exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
            exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
            return;
        } else {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        result = new HashMap<>();
                        result.put("userId", userId);
                        result.put("userType", rs.getString("user_type"));
                        result.put("firstName", rs.getString("first_name"));
                        result.put("lastName", rs.getString("last_name"));
                        result.put("email", rs.getString("email"));
                        result.put("password", rs.getString("password"));
                        PathHandlerProvider.users.put(userId, result);
                        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
                        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
                    } else {
                        Status status = new Status(USER_NOT_FOUND, userId);
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
