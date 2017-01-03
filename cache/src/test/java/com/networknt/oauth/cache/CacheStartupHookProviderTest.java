package com.networknt.oauth.cache;

import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.SqlPredicate;
import com.networknt.oauth.cache.model.User;
import com.networknt.service.SingletonServiceFactory;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by stevehu on 2016-12-27.
 */
public class CacheStartupHookProviderTest {

    @BeforeClass
    public static void runOnceBeforeClass() {
        System.out.println("@BeforeClass - runOnceBeforeClass");
        DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
        try (Connection connection = ds.getConnection()) {
            String schemaResourceName = "/create_h2.sql";
            InputStream in = CacheStartupHookProviderTest.class.getResourceAsStream(schemaResourceName);

            if (in == null) {
                throw new RuntimeException("Failed to load resource: " + schemaResourceName);
            }
            InputStreamReader reader = new InputStreamReader(in, UTF_8);
            RunScript.execute(connection, reader);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Run once, e.g close connection, cleanup
    @AfterClass
    public static void runOnceAfterClass() {
        System.out.println("@AfterClass - runOnceAfterClass");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testClientCache() {
        CacheStartupHookProvider start = new CacheStartupHookProvider();
        start.onStartup();

        final IMap<String, Object> clients = CacheStartupHookProvider.hz.getMap("clients");

        Map<String, Object> client = (Map<String, Object>)clients.get("f7d42348-c647-4efb-a52d-4c5787421e72");
        System.out.println("client = " + client);

        client.put("clientType", "mobile");
        clients.put("f7d42348-c647-4efb-a52d-4c5787421e72", client);
        System.out.println("clients size = " + clients.size());

        clients.delete("f7d42348-c647-4efb-a52d-4c5787421e72");
        System.out.println("clients size = " + clients.size());

        CacheShutdownHookProvider shutdown = new CacheShutdownHookProvider();
        shutdown.onShutdown();

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testServiceCache() {
        CacheStartupHookProvider start = new CacheStartupHookProvider();
        start.onStartup();

        final IMap<String, Object> services = CacheStartupHookProvider.hz.getMap("services");

        Map<String, Object> service = (Map<String, Object>)services.get("AACT0001");
        System.out.println("service = " + service);

        service.put("serviceType", "api");
        services.replace("AACT0001", service);

        System.out.println("services size = " + services.size());

        services.delete("AACT0001");

        System.out.println("services size = " + services.size());

        CacheShutdownHookProvider shutdown = new CacheShutdownHookProvider();
        shutdown.onShutdown();

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUserCache() {
        CacheStartupHookProvider start = new CacheStartupHookProvider();
        start.onStartup();

        final IMap<String, User> users = CacheStartupHookProvider.hz.getMap("users");

        User user = (User)users.get("admin");
        System.out.println("user = " + user);

        user.setUserType(User.UserTypeEnum.fromValue("customer"));

        users.put("admin", user);

        System.out.println("users size = " + users.size());

        // query email as it is indexed.
        String email = "adm%";
        Predicate predicate = new SqlPredicate(String.format("email like %s", email));
        Set<User> uSet = (Set<User>) users.values(predicate);

        System.out.println("uSet = " + uSet);

        users.delete("admin");

        System.out.println("users size = " + users.size());

        CacheShutdownHookProvider shutdown = new CacheShutdownHookProvider();
        shutdown.onShutdown();

    }

    @Test
    public void testCodeCache() {
        CacheStartupHookProvider start = new CacheStartupHookProvider();
        start.onStartup();

        final IMap<String, Object> codes = CacheStartupHookProvider.hz.getMap("codes");
        Map<String, Object> user = new HashMap<>();
        user.put("userId", "admin");
        user.put("userType", "customer");

        Map<String, Object> code = new HashMap<>();
        code.put("code1", user);

        System.out.println("code = " + code);

        codes.put("code1", code);

        System.out.println("codes size = " + codes.size());

        codes.delete("code1");

        System.out.println("codes size = " + codes.size());

        CacheShutdownHookProvider shutdown = new CacheShutdownHookProvider();
        shutdown.onShutdown();

    }

}
