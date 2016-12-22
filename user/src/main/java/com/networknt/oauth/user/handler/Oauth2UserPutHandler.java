package com.networknt.oauth.user.handler;

import com.networknt.body.BodyHandler;
import com.networknt.oauth.user.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class Oauth2UserPutHandler implements HttpHandler {

    static Logger logger = LoggerFactory.getLogger(Oauth2UserPostHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sql = "UPDATE users SET user_type=?, first_name=?, last_name=?, email=?, update_dt=? WHERE user_id = ?";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> user = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, (String)user.get("userType"));
            stmt.setString(2, (String)user.get("firstName"));
            stmt.setString(3, (String)user.get("lastName"));
            stmt.setString(4, (String)user.get("email"));
            stmt.setDate(5, new Date(System.currentTimeMillis()));
            stmt.setString(6, (String)user.get("userId"));
            stmt.executeUpdate();
            PathHandlerProvider.users.put((String)user.get("userId"), user);
        } catch (SQLException e) {
            logger.error("Exception:", e);
            // should handle this exception and return an error message.
            throw e;
        }
    }
}
