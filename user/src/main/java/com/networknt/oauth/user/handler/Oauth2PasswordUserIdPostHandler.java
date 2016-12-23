package com.networknt.oauth.user.handler;

import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.oauth.user.HashUtil;
import com.networknt.oauth.user.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class Oauth2PasswordUserIdPostHandler implements HttpHandler {
    static String INCORRECT_PASSWORD = "ERR12016";
    static String PASSWORD_PASSWORDCONFIRM_NOT_MATCH = "ERR12012";

    static Logger logger = LoggerFactory.getLogger(Oauth2PasswordUserIdPostHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sqlUpdate = "UPDATE users SET password=? WHERE user_id = ?";
    static String sqlSelect = "SELECT * FROM users WHERE user_id = ?";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> body = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String userId = exchange.getQueryParameters().get("userId").getFirst();
        String password = (String)body.get("password");
        String newPassword = (String)body.get("newPassword");
        String newPasswordConfirm = (String)body.get("newPasswordConfirm");
        if(logger.isDebugEnabled()) {
            logger.debug("userId:" + userId + " password:" + password + " newPassword:" + newPassword + " newPasswordConfirm:" + newPasswordConfirm);
        }
        Map<String, Object> user = (Map<String, Object>)PathHandlerProvider.users.get(userId);
        if(user == null) {
            user = selectUser(userId);
        }
        if(!HashUtil.validatePassword(password, (String)user.get("password"))) {
            Status status = new Status(INCORRECT_PASSWORD);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
        if(newPassword.equals(newPasswordConfirm)) {
            String hashedPass = HashUtil.generateStorngPasswordHash(newPassword);

            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sqlUpdate)) {
                stmt.setString(1, hashedPass);
                stmt.setString(2, userId);
                stmt.executeUpdate();
                user.put("password", hashedPass);
                PathHandlerProvider.users.put(userId, user);
            } catch (SQLException e) {
                logger.error("Exception:", e);
                // should handle this exception and return an error message.
                throw e;
            }
        } else {
            Status status = new Status(PASSWORD_PASSWORDCONFIRM_NOT_MATCH, newPassword, newPasswordConfirm);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
    }

    Map<String, Object> selectUser(String userId) throws Exception {
        Map<String, Object> result = null;
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sqlSelect)) {
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
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw e;
        }
        return result;
    }
}
