package com.networknt.oauth.key;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.networknt.config.Config;
import com.networknt.oauth.key.handler.Oauth2KeyKeyIdGetHandler;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;

import java.util.Map;

public class PathHandlerProvider implements HandlerProvider {
    @Override
    public HttpHandler getHandler() {
        HttpHandler handler = Handlers.routing()
            .add(Methods.GET, "/oauth2/key/{keyId}", new Oauth2KeyKeyIdGetHandler())
        ;
        return handler;
    }
}

