package com.networknt.oauth.token;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.networknt.oauth.token.handler.Oauth2TokenPostHandler;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

import java.util.Map;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        HttpHandler handler = Handlers.routing()
            .add(Methods.POST, "/oauth2/token", new Oauth2TokenPostHandler())
        ;
        return handler;
    }
}

