package com.networknt.oauth.user.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
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

public class Oauth2PasswordUserIdPostHandler implements HttpHandler {
    static String INCORRECT_PASSWORD = "ERR12016";
    static String PASSWORD_PASSWORDCONFIRM_NOT_MATCH = "ERR12012";
    static String USER_NOT_FOUND = "ERR12013";

    static Logger logger = LoggerFactory.getLogger(Oauth2PasswordUserIdPostHandler.class);

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> body = (Map)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        String userId = exchange.getQueryParameters().get("userId").getFirst();
        String password = (String)body.get("password");
        String newPassword = (String)body.get("newPassword");
        String newPasswordConfirm = (String)body.get("newPasswordConfirm");

        IMap<String, Object> users = CacheStartupHookProvider.hz.getMap("users");
        Map<String, Object> user = (Map<String, Object>)users.get(userId);
        if(user == null) {
            Status status = new Status(USER_NOT_FOUND, userId);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        } else {
            if(!HashUtil.validatePassword(password, (String)user.get("password"))) {
                Status status = new Status(INCORRECT_PASSWORD);
                exchange.setStatusCode(status.getStatusCode());
                exchange.getResponseSender().send(status.toString());
                return;
            }
            if(newPassword.equals(newPasswordConfirm)) {
                String hashedPass = HashUtil.generateStorngPasswordHash(newPassword);
                user.put("password", hashedPass);
                users.set(userId, user);
            } else {
                Status status = new Status(PASSWORD_PASSWORDCONFIRM_NOT_MATCH, newPassword, newPasswordConfirm);
                exchange.setStatusCode(status.getStatusCode());
                exchange.getResponseSender().send(status.toString());
                return;
            }
        }
    }
}
