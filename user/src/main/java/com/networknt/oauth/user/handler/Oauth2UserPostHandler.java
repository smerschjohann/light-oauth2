package com.networknt.oauth.user.handler;

import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
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

public class Oauth2UserPostHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2UserPostHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sql = "INSERT INTO users (user_id, user_type, first_name, last_name, email, password) VALUES (?, ?, ?, ?, ?, ?)";
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> user = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        // TODO check password and passwordConfirm matches
        String password = (String)user.get("password");
        String passwordConfirm = (String)user.get("passwordConfirm");

        // Hash the password with salt.

        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, (String)user.get("userId"));
            stmt.setString(2, (String)user.get("userType"));
            stmt.setString(3, (String)user.get("firstName"));
            stmt.setString(4, (String)user.get("lastName"));
            stmt.setString(5, (String)user.get("email"));
            stmt.setString(6, password);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw e;
        }
        // return the user object? with password?

        Map<String, Object> examples = new HashMap<String, Object>();
        examples.put("application/json", StringEscapeUtils.unescapeHtml4("{  &quot;firstName&quot; : &quot;aeiou&quot;,  &quot;lastName&quot; : &quot;aeiou&quot;,  &quot;userId&quot; : &quot;aeiou&quot;,  &quot;email&quot; : &quot;aeiou&quot;}"));
        if(examples.size() > 0) {
            exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
            exchange.getResponseSender().send((String)examples.get("application/json"));
        } else {
            exchange.endExchange();
        }
    }
}
