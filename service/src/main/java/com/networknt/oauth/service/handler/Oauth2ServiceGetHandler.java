package com.networknt.oauth.service.handler;

import com.networknt.config.Config;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class Oauth2ServiceGetHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ServiceGetHandler.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String sql = "SELECT * FROM services";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> service = new HashMap<>();

                    service.put("serviceId", rs.getString("service_id"));
                    service.put("serviceType", rs.getString("service_type"));
                    service.put("serviceName", rs.getString("service_name"));
                    service.put("serviceDesc", rs.getString("service_desc"));
                    service.put("scope", rs.getString("scope"));
                    result.add(service);
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw e;
        }
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
    }
}
