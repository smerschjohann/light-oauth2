package com.networknt.oauth.service.handler;

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

public class Oauth2ServiceServiceIdDeleteHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ServiceServiceIdGetHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sql = "DELETE FROM services WHERE service_id = ?";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String serviceId = exchange.getQueryParameters().get("serviceId").getFirst();
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, serviceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Exception:", e);
            // should handle this exception and return an error message.
            throw e;
        }
    }
}
