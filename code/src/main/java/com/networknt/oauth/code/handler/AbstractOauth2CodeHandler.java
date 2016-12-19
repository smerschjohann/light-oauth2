package com.networknt.oauth.code.handler;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.Map;

/**
 * Created by stevehu on 2016-12-18.
 *
 * This class implement client and code cache
 *
 */
public abstract class AbstractOauth2CodeHandler implements HttpHandler {

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

    public abstract void handleRequest(HttpServerExchange exchange) throws Exception;
}
