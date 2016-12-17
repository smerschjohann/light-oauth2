package com.networknt.oauth.token;

import com.networknt.oauth.token.handler.Oauth2TokenPostHandler;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

public class PathHandlerProvider implements HandlerProvider {

    public HttpHandler getHandler() {
        HttpHandler handler = Handlers.routing()
            .add(Methods.POST, "/v1/oauth2/token", new Oauth2TokenPostHandler())
        ;
        return handler;
    }
}

