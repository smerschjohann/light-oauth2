package com.networknt.oauth.service.handler;

import com.networknt.body.BodyHandler;
import com.networknt.oauth.service.PathHandlerProvider;
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

public class Oauth2ServicePutHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ServicePostHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sql = "UPDATE services SET service_type = ?, service_name=?, service_desc=?, scope=?, owner_id=?, update_dt=? WHERE service_id=?";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> service = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, (String)service.get("serviceType"));
            stmt.setString(2, (String)service.get("serviceName"));
            stmt.setString(3, (String)service.get("serviceDesc"));
            stmt.setString(4, (String)service.get("scope"));
            stmt.setString(5, (String)service.get("ownerId"));
            stmt.setDate(6, new Date(System.currentTimeMillis()));
            stmt.setString(7, (String)service.get("serviceId"));
            stmt.executeUpdate();
            PathHandlerProvider.services.put((String)service.get("serviceId"), service);
        } catch (SQLException e) {
            logger.error("Exception:", e);
            // should handle this exception and return an error message.
            throw e;
        }
    }
}
