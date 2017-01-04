package com.networknt.oauth.user.handler;

import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.impl.predicates.LikePredicate;
import com.networknt.config.Config;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.User;
import com.networknt.status.Status;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Map;

/**
 * Created by stevehu on 2017-01-03.
 */
public class Oauth2UserGetHandler implements HttpHandler {
    static final String USER_NOT_FOUND = "ERR12013";
    static Logger logger = LoggerFactory.getLogger(Oauth2UserGetHandler.class);

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");
        Deque<String> userIdDeque = exchange.getQueryParameters().get("userId");
        String userId = userIdDeque == null? "%" : userIdDeque.getFirst() + "%";
        int page = Integer.valueOf(exchange.getQueryParameters().get("page").getFirst()) - 1;
        Deque<String> pageSizeDeque = exchange.getQueryParameters().get("pageSize");
        int pageSize = pageSizeDeque == null? 10 : Integer.valueOf(pageSizeDeque.getFirst());

        // a comparator which helps to sort in descending order of userId field
        Comparator<Map.Entry> comparator = (Comparator<Map.Entry> & Serializable)(e1, e2) -> {
            User u1 = (User) e1.getValue();
            User u2 = (User) e2.getValue();
            return u1.getUserId().compareTo(u2.getUserId());
        };

        LikePredicate likePredicate = new LikePredicate("userId", userId);

        PagingPredicate pagingPredicate = new PagingPredicate(likePredicate, comparator, pageSize);
        pagingPredicate.setPage(page);
        Collection<User> values = users.values(pagingPredicate);

        for (User value : values) {
            value.setPassword(null);
        }
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(values));
    }
}
