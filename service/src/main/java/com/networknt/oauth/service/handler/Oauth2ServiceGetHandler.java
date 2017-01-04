package com.networknt.oauth.service.handler;

import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.impl.predicates.LikePredicate;
import com.networknt.config.Config;
import com.networknt.exception.ApiException;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Service;
import com.networknt.oauth.cache.model.User;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
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

public class Oauth2ServiceGetHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(Oauth2ServiceGetHandler.class);
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        IMap<String, Service> services = CacheStartupHookProvider.hz.getMap("services");

        Deque<String> serviceIdDeque = exchange.getQueryParameters().get("serviceId");
        String serviceId = serviceIdDeque == null? "%" : serviceIdDeque.getFirst() + "%";
        int page = Integer.valueOf(exchange.getQueryParameters().get("page").getFirst()) - 1;
        Deque<String> pageSizeDeque = exchange.getQueryParameters().get("pageSize");
        int pageSize = pageSizeDeque == null? 10 : Integer.valueOf(pageSizeDeque.getFirst());

        // a comparator which helps to sort in descending order of userId field
        Comparator<Map.Entry> comparator = (Comparator<Map.Entry> & Serializable)(e1, e2) -> {
            Service s1 = (Service) e1.getValue();
            Service s2 = (Service) e2.getValue();
            return s1.getServiceId().compareTo(s2.getServiceId());
        };

        LikePredicate likePredicate = new LikePredicate("serviceId", serviceId);

        PagingPredicate pagingPredicate = new PagingPredicate(likePredicate, comparator, pageSize);
        pagingPredicate.setPage(page);
        Collection<Service> values = services.values(pagingPredicate);

        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(values));
    }
}
