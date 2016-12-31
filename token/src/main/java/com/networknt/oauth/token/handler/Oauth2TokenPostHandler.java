package com.networknt.oauth.token.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.security.JwtHelper;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;
import org.jose4j.jwt.JwtClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Oauth2TokenPostHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2TokenPostHandler.class);

    static final String UNABLE_TO_PARSE_FORM_DATA = "ERR12000";
    static final String UNSUPPORTED_GRANT_TYPE = "ERR12001";
    static final String MISSING_AUTHORIZATION_HEADER = "ERR12002";
    static final String INVALID_AUTHORIZATION_HEADER = "ERR12003";
    static final String INVALID_BASIC_CREDENTIALS = "ERR12004";
    static final String JSON_PROCESSING_EXCEPTION = "ERR12005";
    static final String CLIENT_NOT_FOUND = "ERR12014";
    static final String UNAUTHORIZED_CLIENT = "ERR12007";
    static final String INVALID_AUTHORIZATION_CODE = "ERR12008";
    static final String GENERIC_EXCEPTION = "ERR10014";
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
                exchange.getResponseSender().send(mapper.writeValueAsString(handleClientCredentials(exchange)));
            } else if("authorization_code".equals(formMap.get("grant_type"))) {
                exchange.getResponseSender().send(mapper.writeValueAsString(handleAuthorizationCode(exchange, (String)formMap.get("code"))));
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
    private Map<String, Object> handleClientCredentials(HttpServerExchange exchange) throws ApiException {
        // get Authorization header.
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
                    Map<String, Object> client = (Map<String, Object>) CacheStartupHookProvider.hz.getMap("clients").get(clientId);
                    if(client == null) {
                        throw new ApiException(new Status(CLIENT_NOT_FOUND, clientId));
                    } else {
                        String secret = (String)client.get("clientSecret");
                        if(secret.equals(clientSecret)) {
                            String scope = (String)client.get("scope");
                            String jwt;
                            try {
                                jwt = JwtHelper.getJwt(mockCcClaims(clientId, scope));
                            } catch (Exception e) {
                                throw new ApiException(new Status(GENERIC_EXCEPTION, e.getMessage()));
                            }
                            Map<String, Object> resMap = new HashMap<>();
                            resMap.put("access_token", jwt);
                            resMap.put("token_type", "bearer");
                            resMap.put("expires_in", 600);
                            return resMap;
                        } else {
                            throw new ApiException(new Status(UNAUTHORIZED_CLIENT));
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleAuthorizationCode(HttpServerExchange exchange, String code) throws ApiException {
        if(logger.isDebugEnabled()) logger.debug("code = " + code);
        // get Authorization header.
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

                    Map<String, Object> client = (Map<String, Object>)CacheStartupHookProvider.hz.getMap("clients").get(clientId);
                    if(client == null) {
                        throw new ApiException(new Status(CLIENT_NOT_FOUND, clientId));
                    } else {
                        String secret = (String)client.get("clientSecret");
                        if(secret.equals(clientSecret)) {
                            String userId = (String)CacheStartupHookProvider.hz.getMap("codes").remove(code);
                            if(userId != null) {
                                Map<String, Object> user = (Map<String, Object>)CacheStartupHookProvider.hz.getMap("users").get(userId);
                                String scope = (String)client.get("scope");
                                String jwt;
                                try {
                                    jwt = JwtHelper.getJwt(mockAcClaims(clientId, scope, userId, (String)user.get("userType")));
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
                        } else {
                            throw new ApiException(new Status(UNAUTHORIZED_CLIENT));
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


    public JwtClaims mockCcClaims(String clientId, String scopeString) {
        JwtClaims claims = JwtHelper.getDefaultJwtClaims();
        claims.setClaim("client_id", clientId);
        List<String> scope = Arrays.asList(scopeString.split("\\s+"));
        claims.setStringListClaim("scope", scope); // multi-valued claims work too and will end up as a JSON array
        return claims;
    }

    public JwtClaims mockAcClaims(String clientId, String scopeString, String userId, String userType) {
        JwtClaims claims = JwtHelper.getDefaultJwtClaims();
        claims.setClaim("user_id", userId);
        claims.setClaim("user_type", userType);
        claims.setClaim("client_id", clientId);
        List<String> scope = Arrays.asList(scopeString.split("\\s+"));
        claims.setStringListClaim("scope", scope); // multi-valued claims work too and will end up as a JSON array
        return claims;
    }

    public static String decodeCredentials(String cred) {
        return new String(org.apache.commons.codec.binary.Base64.decodeBase64(cred), UTF_8);
    }
}
