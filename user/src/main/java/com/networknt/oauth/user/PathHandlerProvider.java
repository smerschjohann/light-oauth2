package com.networknt.oauth.user;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.networknt.oauth.user.handler.*;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

import java.util.Map;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        HttpHandler handler = Handlers.routing()
            .add(Methods.POST, "/oauth2/password/{userId}", new Oauth2PasswordUserIdPostHandler())
            .add(Methods.POST, "/oauth2/user", new Oauth2UserPostHandler())
            .add(Methods.PUT, "/oauth2/user", new Oauth2UserPutHandler())
            .add(Methods.DELETE, "/oauth2/user/{userId}", new Oauth2UserUserIdDeleteHandler())
            .add(Methods.GET, "/oauth2/user/{userId}", new Oauth2UserUserIdGetHandler())
        ;
        return handler;
    }
}

