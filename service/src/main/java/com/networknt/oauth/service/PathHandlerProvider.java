package com.networknt.oauth.service;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.networknt.oauth.service.handler.*;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;

import java.util.Map;

public class PathHandlerProvider implements HandlerProvider {

    public static Map<String, Object> users;
    public static Map<String, Object> clients;
    public static Map<String, Object> codes;
    public static Map<String, Object> services;

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
            .add(Methods.GET, "/oauth2/service", new Oauth2ServiceGetHandler())
            .add(Methods.POST, "/oauth2/service", new Oauth2ServicePostHandler())
            .add(Methods.PUT, "/oauth2/service", new Oauth2ServicePutHandler())
            .add(Methods.DELETE, "/oauth2/service/{serviceId}", new Oauth2ServiceServiceIdDeleteHandler())
            .add(Methods.GET, "/oauth2/service/{serviceId}", new Oauth2ServiceServiceIdGetHandler())
        ;
        return handler;
    }
}

