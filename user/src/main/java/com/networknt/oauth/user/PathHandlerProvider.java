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

    public static IMap<String, Object> users;
    public static IMap<String, Object> clients;
    public static IMap<String, Object> codes;
    public static IMap<String, Object> services;

    static {
        Config cfg = new Config();
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(cfg);
        users = hz.getMap("users");
        clients = hz.getMap("clients");
        codes = hz.getMap("codes");
        services = hz.getMap("services");
    }

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

