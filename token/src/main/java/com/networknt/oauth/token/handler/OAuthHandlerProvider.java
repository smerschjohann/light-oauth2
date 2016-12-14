package com.networknt.oauth.token.handler;

import com.networknt.config.Config;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;


public class OAuthHandlerProvider implements HandlerProvider {

    public HttpHandler getHandler() {
        return Handlers.path().addPrefixPath("/oauth2/token",
                new TokenHandler(Config.getInstance().getMapper()));
    }
}
