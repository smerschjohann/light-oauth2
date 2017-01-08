package com.networknt.oauth.token.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.IMap;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.User;
import com.networknt.security.JwtHelper;
import com.networknt.status.Status;
import com.networknt.utility.HashUtil;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Oauth2TokenPostHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(Oauth2TokenPostHandler.class);

    private static final String UNABLE_TO_PARSE_FORM_DATA = "ERR12000";
    private static final String UNSUPPORTED_GRANT_TYPE = "ERR12001";
    private static final String MISSING_AUTHORIZATION_HEADER = "ERR12002";
    private static final String INVALID_AUTHORIZATION_HEADER = "ERR12003";
    private static final String INVALID_BASIC_CREDENTIALS = "ERR12004";
    private static final String JSON_PROCESSING_EXCEPTION = "ERR12005";
    private static final String CLIENT_NOT_FOUND = "ERR12014";
    private static final String UNAUTHORIZED_CLIENT = "ERR12007";
    private static final String INVALID_AUTHORIZATION_CODE = "ERR12008";
    private static final String GENERIC_EXCEPTION = "ERR10014";
    private static final String RUNTIME_EXCEPTION = "ERR10010";
    private static final String USERNAME_REQUIRED = "ERR12022";
    private static final String PASSWORD_REQUIRED = "ERR12023";
    private static final String INCORRECT_PASSWORD = "ERR12016";
    private static final String NOT_TRUSTED_CLIENT = "ERR12024";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        ObjectMapper mapper = Config.getInstance().getMapper();
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        Map<String, Object> formMap = new HashMap<>();

        final FormParserFactory parserFactory = FormParserFactory.builder().build();
        final FormDataParser parser = parserFactory.createParser(exchange);
        try {
            FormData data = parser.parseBlocking();
            for (String fd : data) {
                for (FormData.FormValue val : data.get(fd)) {
                    //logger.debug("fd = " + fd + " value = " + val.getValue());
                    formMap.put(fd, val.getValue());
                }
            }
        } catch (Exception e) {
            Status status = new Status(UNABLE_TO_PARSE_FORM_DATA, e.getMessage());
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
        try {
            if("client_credentials".equals(formMap.get("grant_type"))) {
                exchange.getResponseSender().send(mapper.writeValueAsString(handleClientCredentials(exchange, (String)formMap.get("scope"))));
            } else if("authorization_code".equals(formMap.get("grant_type"))) {
                exchange.getResponseSender().send(mapper.writeValueAsString(handleAuthorizationCode(exchange, (String)formMap.get("code"), (String)formMap.get("scope"))));
            } else if("password".equals(formMap.get("grant_type"))) {
                exchange.getResponseSender().send(mapper.writeValueAsString(handlePassword(exchange, (String)formMap.get("username"), (String)formMap.get("password"), (String)formMap.get("scope"))));
            } else {
                Status status = new Status(UNSUPPORTED_GRANT_TYPE, formMap.get("grant_type"));
                exchange.setStatusCode(status.getStatusCode());
                exchange.getResponseSender().send(status.toString());
            }
        } catch (JsonProcessingException e) {
            Status status = new Status(JSON_PROCESSING_EXCEPTION, e.getMessage());
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
        } catch (ApiException e) {
            exchange.setStatusCode(e.getStatus().getStatusCode());
            exchange.getResponseSender().send(e.getStatus().toString());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleClientCredentials(HttpServerExchange exchange, String scope) throws ApiException {
        if(logger.isDebugEnabled()) logger.debug("scope = " + scope);
        Client client = authenticateClient(exchange);
        if(client != null) {
            if(scope == null) scope = client.getScope();
            String jwt;
            try {
                jwt = JwtHelper.getJwt(mockCcClaims(client.getClientId(), scope));
            } catch (Exception e) {
                throw new ApiException(new Status(GENERIC_EXCEPTION, e.getMessage()));
            }
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("access_token", jwt);
            resMap.put("token_type", "bearer");
            resMap.put("expires_in", 600);
            return resMap;

        }
        return new HashMap<>(); // return an empty hash map. this is actually not reachable at all.
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleAuthorizationCode(HttpServerExchange exchange, String code, String scope) throws ApiException {
        if(logger.isDebugEnabled()) logger.debug("code = " + code + " scope = " + scope);
        Client client = authenticateClient(exchange);
        if(client != null) {
            String userId = (String)CacheStartupHookProvider.hz.getMap("codes").remove(code);
            if(userId != null) {
                IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
                User user = users.get(userId);
                if(scope == null) scope = client.getScope();
                String jwt;
                try {
                    jwt = JwtHelper.getJwt(mockAcClaims(client.getClientId(), scope, userId, user.getUserType().toString()));
                } catch (Exception e) {
                    throw new ApiException(new Status(GENERIC_EXCEPTION, e.getMessage()));
                }
                Map<String, Object> resMap = new HashMap<>();
                resMap.put("access_token", jwt);
                resMap.put("token_type", "bearer");
                resMap.put("expires_in", 600);
                return resMap;
            } else {
                throw new ApiException(new Status(INVALID_AUTHORIZATION_CODE, code));
            }
        }
        return new HashMap<>(); // return an empty hash map. this is actually not reachable at all.
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handlePassword(HttpServerExchange exchange, String userId, String password, String scope) throws ApiException {
        if(logger.isDebugEnabled()) logger.debug("userId = " + userId + " scope = " + scope);
        Client client = authenticateClient(exchange);
        if(client != null) {
            // authenticate user with credentials
            if(userId != null) {
                if(password != null) {
                    IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
                    User user = users.get(userId);
                    // match password
                    try {
                        if(HashUtil.validatePassword(password, user.getPassword())) {
                            // make sure that client is trusted
                            if(client.getClientType() == Client.ClientTypeEnum.TRUSTED) {
                                if(scope == null) scope = client.getScope(); // use the default scope defined in client if scope is not passed in
                                String jwt = JwtHelper.getJwt(mockAcClaims(client.getClientId(), scope, userId, user.getUserType().toString()));
                                Map<String, Object> resMap = new HashMap<>();
                                resMap.put("access_token", jwt);
                                resMap.put("token_type", "bearer");
                                resMap.put("expires_in", 600);
                                return resMap;
                            } else {
                                throw new ApiException(new Status(NOT_TRUSTED_CLIENT));
                            }
                        } else {
                            throw new ApiException(new Status(INCORRECT_PASSWORD));
                        }
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException | JoseException e) {
                        throw new ApiException(new Status(GENERIC_EXCEPTION, e.getMessage()));
                    }
                } else {
                    throw new ApiException(new Status(PASSWORD_REQUIRED));
                }
            } else {
                throw new ApiException(new Status(USERNAME_REQUIRED));
            }
        }
        return new HashMap<>(); // return an empty hash map. this is actually not reachable at all.
    }

    private Client authenticateClient(HttpServerExchange exchange) throws ApiException {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if(auth == null || auth.trim().length() == 0) {
            throw new ApiException(new Status(MISSING_AUTHORIZATION_HEADER));
        } else {
            String basic = auth.substring(0, 5);
            if("BASIC".equalsIgnoreCase(basic)) {
                String credentials = auth.substring(6);
                String clientId;
                String clientSecret;
                int pos = credentials.indexOf(':');
                if(pos == -1) {
                    credentials = decodeCredentials(credentials);
                }
                pos = credentials.indexOf(':');
                if(pos != -1) {
                    clientId = credentials.substring(0, pos);
                    clientSecret = credentials.substring(pos + 1);
                    IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
                    Client client = clients.get(clientId);
                    if(client == null) {
                        throw new ApiException(new Status(CLIENT_NOT_FOUND, clientId));
                    } else {
                        try {
                            if(HashUtil.validatePassword(clientSecret, client.getClientSecret())) {
                                return client;
                            } else {
                                throw new ApiException(new Status(UNAUTHORIZED_CLIENT));
                            }
                        } catch ( NoSuchAlgorithmException | InvalidKeySpecException e) {
                            logger.error("Exception:", e);
                            throw new ApiException(new Status(RUNTIME_EXCEPTION));
                        }
                    }
                } else {
                    throw new ApiException(new Status(INVALID_BASIC_CREDENTIALS, credentials));
                }
            } else {
                throw new ApiException(new Status(INVALID_AUTHORIZATION_HEADER, auth));
            }
        }
    }

    private JwtClaims mockCcClaims(String clientId, String scopeString) {
        JwtClaims claims = JwtHelper.getDefaultJwtClaims();
        claims.setClaim("client_id", clientId);
        List<String> scope = Arrays.asList(scopeString.split("\\s+"));
        claims.setStringListClaim("scope", scope); // multi-valued claims work too and will end up as a JSON array
        return claims;
    }

    private JwtClaims mockAcClaims(String clientId, String scopeString, String userId, String userType) {
        JwtClaims claims = JwtHelper.getDefaultJwtClaims();
        claims.setClaim("user_id", userId);
        claims.setClaim("user_type", userType);
        claims.setClaim("client_id", clientId);
        List<String> scope = Arrays.asList(scopeString.split("\\s+"));
        claims.setStringListClaim("scope", scope); // multi-valued claims work too and will end up as a JSON array
        return claims;
    }

    private static String decodeCredentials(String cred) {
        return new String(org.apache.commons.codec.binary.Base64.decodeBase64(cred), UTF_8);
    }
}
