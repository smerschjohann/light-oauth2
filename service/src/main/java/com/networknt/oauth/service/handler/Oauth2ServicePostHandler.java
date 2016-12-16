package com.networknt.oauth.service.handler;

import com.networknt.body.BodyHandler;
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

public class Oauth2ServicePostHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ServicePostHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sql = "INSERT INTO services (service_id, service_type, service_name, service_desc, scope, create_dt) VALUES (?, ?, ?, ?, ?, ?)";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> service = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String serviceId = (String)service.get("serviceId");
        // TODO make sure serviceId doesn't exist in database

        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, serviceId);
            stmt.setString(2, (String)service.get("serviceType"));
            stmt.setString(3, (String)service.get("serviceName"));
            stmt.setString(4, (String)service.get("serviceDesc"));
            stmt.setString(5, (String)service.get("scope"));
            stmt.setDate(6, new Date(System.currentTimeMillis()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Exception:", e);
            // should handle this exception and return an error message.
            throw e;
        }
    }
}
