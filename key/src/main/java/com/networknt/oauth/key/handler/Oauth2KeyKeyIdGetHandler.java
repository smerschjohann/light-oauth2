package com.networknt.oauth.key.handler;

import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.oauth.key.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import com.networknt.utility.HashUtil;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.FlexBase64;
import io.undertow.util.HeaderValues;
import io.undertow.util.HttpString;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import static io.undertow.util.Headers.AUTHORIZATION;
import static io.undertow.util.Headers.BASIC;

public class Oauth2KeyKeyIdGetHandler implements HttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2KeyKeyIdGetHandler.class);

    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);

    static final String CONFIG_SECURITY = "security";
    static final String CONFIG_JWT = "jwt";
    static final String CONFIG_CERTIFICATE = "certificate";
    static final String KEY_NOT_FOUND = "ERR12017";
    static final String MISSING_AUTHORIZATION_HEADER = "ERR12002";
    static final String CLIENT_NOT_FOUND = "ERR12014";
    static final String RUNTIME_EXCEPTION = "ERR10010";
    static final String UNAUTHORIZED_CLIENT = "ERR12007";
    static final String SQL_EXCEPTION = "ERR10017";

    private static final String BASIC_PREFIX = BASIC + " ";
    private static final String LOWERCASE_BASIC_PREFIX = BASIC_PREFIX.toLowerCase(Locale.ENGLISH);
    private static final int PREFIX_LENGTH = BASIC_PREFIX.length();
    private static final String COLON = ":";

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // check if client_id and client_secret in header are valid pair.
        HeaderValues values = exchange.getRequestHeaders().get(AUTHORIZATION);
        String authHeader = null;
        if(values != null) {
            authHeader = values.getFirst();
        } else {
            Status status = new Status(MISSING_AUTHORIZATION_HEADER);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
        if(authHeader == null) {
            Status status = new Status(MISSING_AUTHORIZATION_HEADER);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }

        String keyId = exchange.getQueryParameters().get("keyId").getFirst();
        if(logger.isDebugEnabled()) logger.debug("keyId = " + keyId);
        // find the location of the certificate
        Map<String, Object> config = Config.getInstance().getJsonMapConfig(CONFIG_SECURITY);
        Map<String, Object> jwtConfig = (Map<String, Object>)config.get(CONFIG_JWT);
        Map<String, Object> certificateConfig = (Map<String, Object>)jwtConfig.get(CONFIG_CERTIFICATE);
        // find the path for certificate file
        String filename = (String)certificateConfig.get(keyId);
        String content = Config.getInstance().getStringFromFile(filename);
        if(logger.isDebugEnabled()) logger.debug("certificate = " + content);
        if(content != null) {
            exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/text");
            exchange.getResponseSender().send(content);
        } else {
            logger.info("Certificate " + Encode.forJava(filename) + " not found.");
            Status status = new Status(KEY_NOT_FOUND, keyId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
    }

    private String authenticate(String authHeader) throws ApiException {
        String result = null;
        if (authHeader.toLowerCase(Locale.ENGLISH).startsWith(LOWERCASE_BASIC_PREFIX)) {
            String base64Challenge = authHeader.substring(PREFIX_LENGTH);
            String plainChallenge = null;
            try {
                ByteBuffer decode = FlexBase64.decode(base64Challenge);
                // assume charset is UTF_8
                Charset charset = StandardCharsets.UTF_8;
                plainChallenge = new String(decode.array(), decode.arrayOffset(), decode.limit(), charset);
                logger.debug("Found basic auth header %s (decoded using charset %s) in %s", plainChallenge, charset, authHeader);
                int colonPos;
                if (plainChallenge != null && (colonPos = plainChallenge.indexOf(COLON)) > -1) {
                    String clientId = plainChallenge.substring(0, colonPos);
                    String clientSecret = plainChallenge.substring(colonPos + 1);
                    // match with db/cached user credentials.
                    Map<String, Object> client = (Map<String, Object>) PathHandlerProvider.clients.get(clientId);
                    if(client == null) {
                        client = getClient(clientId);
                    }
                    if(client == null) {
                        throw new ApiException(new Status(CLIENT_NOT_FOUND, clientId));
                    }
                    if(clientSecret != null && !clientSecret.equals(client.get("clientSecret"))) {
                        throw new ApiException(new Status(UNAUTHORIZED_CLIENT));
                    }
                    result = clientId;
                }
            } catch (IOException e) {
                logger.error("Exception:", e);
                throw new ApiException(new Status(RUNTIME_EXCEPTION));
            }
        }
        return result;
    }

    private Map<String, Object> getClient(String clientId) throws ApiException {
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
            throw new ApiException(new Status(SQL_EXCEPTION, e.getMessage()));
        }
        return client;
    }

}
