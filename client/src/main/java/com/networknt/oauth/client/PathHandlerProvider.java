package com.networknt.oauth.client;

import com.networknt.oauth.client.handler.*;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

public class PathHandlerProvider implements HandlerProvider {

    public HttpHandler getHandler() {
        HttpHandler handler = Handlers.routing()
            .add(Methods.DELETE, "/v1/oauth2/client/{clientId}", new Oauth2ClientClientIdDeleteHandler())
            .add(Methods.GET, "/v1/oauth2/client/{clientId}", new Oauth2ClientClientIdGetHandler())
            .add(Methods.GET, "/v1/oauth2/client", new Oauth2ClientGetHandler())
            .add(Methods.POST, "/v1/oauth2/client", new Oauth2ClientPostHandler())
            .add(Methods.PUT, "/v1/oauth2/client", new Oauth2ClientPutHandler())
        ;
        return handler;
    }
}

