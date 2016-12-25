package com.networknt.oauth.code.handler;

import com.networknt.oauth.code.PathHandlerProvider;
import com.networknt.oauth.code.auth.Authentication;
import com.networknt.status.Status;
import com.networknt.utility.Util;
import io.undertow.server.HttpHandler;
import io.undertow.util.StatusCodes;
import io.undertow.server.HttpServerExchange;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * This handler is for get request, the credentials might in header or just query parameters.
 * Need to check client info in order to find out which class to handle the authentication
 * clients are cached so that it has better performance. If client_id cannot be found in cache,
 * go to db to get it. It must be something added recently and not in cache yet.
 *
 */
public class Oauth2CodeGetHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2CodeGetHandler.class);
    static final String INVALID_CODE_REQUEST = "ERR12009";
    static final String CLIENT_NOT_FOUND = "ERR12014";
    static final String MISSING_AUTHORIZATION_HEADER = "ERR12002";

    static final String DEFAULT_AUTHENTICATE_CLASS = "com.networknt.oauth.code.auth.BasicAuthentication";

    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

        // parse all the parameters here as this is a redirected get request.
        Map<String, String> params = new HashMap<String, String>();
        Map<String, Deque<String>> pnames = exchange.getQueryParameters();
        for (Map.Entry<String, Deque<String>> entry : pnames.entrySet()) {
            String pname = entry.getKey();
            Iterator<String> pvalues = entry.getValue().iterator();
            if(pvalues.hasNext()) {
                params.put(pname, pvalues.next());
            }
        }
        if(logger.isDebugEnabled()) logger.debug("params", params);
        String responseType = params.get("response_type");
        String clientId = params.get("client_id");
        if(responseType == null || clientId == null) {
            Status status = new Status(INVALID_CODE_REQUEST);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        } else {
            // check if the client_id is valid
            Map<String, Object> client = (Map<String, Object>)PathHandlerProvider.clients.get(clientId);
            if(client == null) {
                if(logger.isDebugEnabled()) logger.debug("load client from db");
                client = getClient(clientId);
            }
            if(client == null) {
                Status status = new Status(CLIENT_NOT_FOUND, clientId);
                exchange.setStatusCode(status.getStatusCode());
                exchange.getResponseSender().send(status.toString());
                return;
            } else {
                String clazz = (String)client.get("authenticateClass");
                if(clazz == null) clazz = DEFAULT_AUTHENTICATE_CLASS;
                Authentication auth = (Authentication)Class.forName(clazz).newInstance();
                String userId = auth.authenticate(exchange);
                if(userId == null) {
                    Status status = new Status(MISSING_AUTHORIZATION_HEADER, clientId);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                    return;
                }
                if(logger.isDebugEnabled()) logger.debug("User is authenticated " + userId);
                // generate auth code
                String code = Util.getUUID();
                PathHandlerProvider.codes.put(code, userId);
                String redirectUrl = params.get("redirect_url");
                if(redirectUrl == null) {
                    redirectUrl = (String)client.get("redirectUrl");
                }
                redirectUrl = redirectUrl + "?code=" + code;
                if(logger.isDebugEnabled()) logger.debug("redirectUrl = " + redirectUrl);
                // now redirect here.
                exchange.setStatusCode(StatusCodes.FOUND);
                exchange.getResponseHeaders().put(Headers.LOCATION, redirectUrl);
                exchange.endExchange();
            }
        }
    }

    private Map<String, Object> getClient(String clientId) throws Exception {
        Map<String, Object> client = null;
        String sql = "SELECT * FROM clients WHERE client_id = ?";
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, clientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    client = new HashMap<>();
                    client.put("clientId", clientId);
                    client.put("clientSecret", rs.getString("client_secret"));
                    client.put("clientType", rs.getString("client_type"));
                    client.put("clientName", rs.getString("client_name"));
                    client.put("clientDesc", rs.getString("client_desc"));
                    client.put("scope", rs.getString("scope"));
                    client.put("redirectUrl", rs.getString("redirect_url"));
                    client.put("authenticateClass", rs.getString("authenticate_class"));
                    client.put("ownerId", rs.getString("owner_id"));
                    client.put("createDt", rs.getDate("create_dt"));
                    client.put("updateDt", rs.getDate("update_dt"));
                    PathHandlerProvider.clients.put(clientId, client);
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw e;
        }
        return client;
    }

}
