package com.networknt.oauth.client.handler;

import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.impl.predicates.LikePredicate;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.User;
import com.networknt.service.SingletonServiceFactory;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Oauth2ClientGetHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2ClientGetHandler.class);

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        IMap<String, Client> clients = CacheStartupHookProvider.hz.getMap("clients");
        Deque<String> clientNameDeque = exchange.getQueryParameters().get("clientName");
        String clientName = clientNameDeque == null? "%" : clientNameDeque.getFirst() + "%";
        int page = Integer.valueOf(exchange.getQueryParameters().get("page").getFirst()) - 1;
        Deque<String> pageSizeDeque = exchange.getQueryParameters().get("pageSize");
        int pageSize = pageSizeDeque == null? 10 : Integer.valueOf(pageSizeDeque.getFirst());

        // a comparator which helps to sort in order of clientName field
        Comparator<Map.Entry> comparator = (Comparator<Map.Entry> & Serializable)(e1, e2) -> {
            Client c1 = (Client) e1.getValue();
            Client c2 = (Client) e2.getValue();
            return c1.getClientName().compareTo(c2.getClientName());
        };

        LikePredicate likePredicate = new LikePredicate("clientName", clientName);

        PagingPredicate pagingPredicate = new PagingPredicate(likePredicate, comparator, pageSize);
        pagingPredicate.setPage(page);
        Collection<Client> values = clients.values(pagingPredicate);

        for (Client value : values) {
            value.setClientSecret(null);
        }
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(values));
    }
}
