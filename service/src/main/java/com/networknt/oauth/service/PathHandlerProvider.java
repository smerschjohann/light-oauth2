package com.networknt.oauth.service;

import com.networknt.oauth.service.handler.*;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

public class PathHandlerProvider implements HandlerProvider {

    public HttpHandler getHandler() {
        HttpHandler handler = Handlers.routing()
            .add(Methods.GET, "/v1/oauth2/service", new Oauth2ServiceGetHandler())
            .add(Methods.POST, "/v1/oauth2/service", new Oauth2ServicePostHandler())
            .add(Methods.PUT, "/v1/oauth2/service", new Oauth2ServicePutHandler())
            .add(Methods.DELETE, "/v1/oauth2/service/{serviceId}", new Oauth2ServiceServiceIdDeleteHandler())
            .add(Methods.GET, "/v1/oauth2/service/{serviceId}", new Oauth2ServiceServiceIdGetHandler())
        ;
        return handler;
    }
}

