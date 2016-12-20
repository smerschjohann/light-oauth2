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

public class Oauth2UserPostHandler implements HttpHandler {
    static String PASSWORD_OR_PASSWORDCONFIRM_EMPTY = "ERR12011";
    static String PASSWORD_PASSWORDCONFIRM_NOT_MATCH = "ERR12012";


    static Logger logger = LoggerFactory.getLogger(Oauth2UserPostHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sql = "INSERT INTO users (user_id, user_type, first_name, last_name, email, password) VALUES (?, ?, ?, ?, ?, ?)";
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> user = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String password = (String)user.get("password");
        String passwordConfirm = (String)user.get("passwordConfirm");
        if(password != null && password.length() > 0 && passwordConfirm != null && passwordConfirm.length() > 0) {
            // check if there are the same
            if(password.equals(passwordConfirm)) {
                // hash the password with salt.
                String hashedPass = HashUtil.generateStorngPasswordHash(password);
                try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, (String)user.get("userId"));
                    stmt.setString(2, (String)user.get("userType"));
                    stmt.setString(3, (String)user.get("firstName"));
                    stmt.setString(4, (String)user.get("lastName"));
                    stmt.setString(5, (String)user.get("email"));
                    stmt.setString(6, hashedPass);
                    stmt.executeUpdate();
                    user.remove("passwordConfirm");
                    user.put("password", hashedPass);
                    PathHandlerProvider.users.putIfAbsent((String)user.get("userId"), user);
                } catch (SQLException e) {
                    logger.error("Exception:", e);
                    throw e;
                }
            } else {
                // password and passwordConfirm not match.
                Status status = new Status(PASSWORD_PASSWORDCONFIRM_NOT_MATCH, password, passwordConfirm);
                exchange.setStatusCode(status.getStatusCode());
                exchange.getResponseSender().send(status.toString());
                return;
            }
        } else {
            // error password or passwordConform is empty
            Status status = new Status(PASSWORD_OR_PASSWORDCONFIRM_EMPTY, password, passwordConfirm);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
    }
}
