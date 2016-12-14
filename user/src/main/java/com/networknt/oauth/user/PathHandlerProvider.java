package com.networknt.oauth.user;

import com.networknt.oauth.user.handler.*;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

public class PathHandlerProvider implements HandlerProvider {

    public HttpHandler getHandler() {
        HttpHandler handler = Handlers.routing()
            .add(Methods.POST, "/v1/oauth2/password/{userId}", new Oauth2PasswordUserIdPostHandler())
            .add(Methods.POST, "/v1/oauth2/user", new Oauth2UserPostHandler())
            .add(Methods.PUT, "/v1/oauth2/user", new Oauth2UserPutHandler())
            .add(Methods.DELETE, "/v1/oauth2/user/{userId}", new Oauth2UserUserIdDeleteHandler())
            .add(Methods.GET, "/v1/oauth2/user/{userId}", new Oauth2UserUserIdGetHandler())
        ;
        return handler;
    }
}

