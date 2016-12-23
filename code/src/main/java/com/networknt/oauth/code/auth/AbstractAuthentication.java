package com.networknt.oauth.code.auth;

import com.networknt.exception.ApiException;
import com.networknt.oauth.code.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stevehu on 2016-12-23.
 */
public abstract class AbstractAuthentication implements Authentication {

    static Logger logger = LoggerFactory.getLogger(AbstractAuthentication.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sqlSelect = "SELECT * FROM users WHERE user_id = ?";

    public abstract String authenticate(HttpServerExchange exchange) throws ApiException;

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
