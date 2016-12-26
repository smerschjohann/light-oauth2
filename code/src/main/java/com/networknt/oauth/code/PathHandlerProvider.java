package com.networknt.oauth.code;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.networknt.oauth.code.handler.MapIdentityManager;
import com.networknt.oauth.code.handler.Oauth2CodeGetHandler;
import com.networknt.oauth.code.handler.Oauth2CodePostHandler;
import com.networknt.server.HandlerProvider;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import io.undertow.Handlers;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.impl.BasicAuthenticationMechanism;
import io.undertow.server.HttpHandler;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathHandlerProvider implements HandlerProvider {
    Logger logger = LoggerFactory.getLogger(PathHandlerProvider.class);
    public static Map<String, Object> users;
    public static Map<String, Object> clients;
    public static Map<String, Object> codes;
    public static Map<String, Object> services;

    static {
        Config cfg = new Config();
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(cfg);
        users = hz.getMap("users");
        clients = hz.getMap("clients");
        codes = hz.getMap("codes");
        services = hz.getMap("services");
    }

    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);


    public HttpHandler getHandler() {
        Map users = getUser();

        final IdentityManager identityManager = new MapIdentityManager(users);

        HttpHandler handler = Handlers.routing()
            .add(Methods.GET, "/oauth2/code", addSecurity(new Oauth2CodeGetHandler(), identityManager))
            .add(Methods.POST, "/oauth2/code", new Oauth2CodePostHandler())
        ;
        return handler;
    }

    private static HttpHandler addSecurity(final HttpHandler toWrap, final IdentityManager identityManager) {
        HttpHandler handler = toWrap;
        handler = new AuthenticationCallHandler(handler);
        handler = new AuthenticationConstraintHandler(handler);
        final List<AuthenticationMechanism> mechanisms = Collections.<AuthenticationMechanism>singletonList(new BasicAuthenticationMechanism("My Realm"));
        handler = new AuthenticationMechanismsHandler(handler, mechanisms);
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, identityManager, handler);
        return handler;
    }

    private Map<String, Object> getUser() {
        Map<String, Object> users = null;
        String sql = "SELECT * FROM users";
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users = new HashMap<>();
                    Map<String, Object> user = new HashMap<>();
                    user.put("userId", rs.getString("user_id"));
                    user.put("userType", rs.getString("user_type"));
                    user.put("email", rs.getString("email"));
                    user.put("password", rs.getString("password"));
                    users.put((String)user.get("userId"), user);
                }
                //PathHandlerProvider.users.putAll(users);
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            // this is the best effort basis and it won't get code if users are not loaded.
            //throw e;
        }
        return users;
    }
}

