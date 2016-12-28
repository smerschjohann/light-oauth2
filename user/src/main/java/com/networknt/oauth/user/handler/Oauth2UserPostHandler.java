package com.networknt.oauth.user.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.utility.HashUtil;
import com.networknt.oauth.user.PathHandlerProvider;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class Oauth2UserPostHandler implements HttpHandler {
    static String PASSWORD_OR_PASSWORDCONFIRM_EMPTY = "ERR12011";
    static String PASSWORD_PASSWORDCONFIRM_NOT_MATCH = "ERR12012";
    static final String USER_ID_EXISTS = "ERR12020";

    static Logger logger = LoggerFactory.getLogger(Oauth2UserPostHandler.class);

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> user = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String password = (String)user.get("password");
        String passwordConfirm = (String)user.get("passwordConfirm");
        if(password != null && password.length() > 0 && passwordConfirm != null && passwordConfirm.length() > 0) {
            // check if there are the same
            if(password.equals(passwordConfirm)) {
                // hash the password with salt.
                String hashedPass = HashUtil.generateStorngPasswordHash(password);
                user.put("password", hashedPass);
                user.remove("passwordConfirm");
                String userId = (String)user.get("userId");

                IMap<String, Object> users = CacheStartupHookProvider.hz.getMap("users");
                if(users.get(userId) == null) {
                    users.set(userId, user);
                } else {
                    Status status = new Status(USER_ID_EXISTS, userId);
                    exchange.setStatusCode(status.getStatusCode());
                    exchange.getResponseSender().send(status.toString());
                    return;
                }
            } else {
                // password and passwordConfirm not match.
                Status status = new Status(PASSWORD_PASSWORDCONFIRM_NOT_MATCH, password, passwordConfirm);
                exchange.setStatusCode(status.getStatusCode());
                exchange.getResponseSender().send(status.toString());
                return;
            }
        } else {
            // error password or passwordConform is empty
            Status status = new Status(PASSWORD_OR_PASSWORDCONFIRM_EMPTY, password, passwordConfirm);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
    }
}
