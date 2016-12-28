package com.networknt.oauth.code;

import com.hazelcast.core.IMap;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.code.handler.MapIdentityManager;
import com.networknt.oauth.code.handler.Oauth2CodeGetHandler;
import com.networknt.oauth.code.handler.Oauth2CodePostHandler;
import com.networknt.server.HandlerProvider;
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
import io.undertow.util.Methods;
import java.util.Collections;
import java.util.List;

public class PathHandlerProvider implements HandlerProvider {
    public HttpHandler getHandler() {
        IMap<String, Object> users = CacheStartupHookProvider.hz.getMap("users");
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
}

