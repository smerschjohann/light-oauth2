package com.networknt.oauth.token.handler;

import com.networknt.server.Server;
import com.networknt.service.SingletonServiceFactory;
import org.h2.tools.RunScript;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class TestServer extends ExternalResource {
    static final Logger logger = LoggerFactory.getLogger(TestServer.class);

    private static AtomicInteger refCount = new AtomicInteger(0);
    private static Server server;

    private static final TestServer instance  = new TestServer();

    public static TestServer getInstance () {
        return instance;
    }

    private TestServer() {
        DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
        try (Connection connection = ds.getConnection()) {
            String schemaResourceName = "/create_h2.sql";
            InputStream in = TestServer.class.getResourceAsStream(schemaResourceName);

            if (in == null) {
                throw new RuntimeException("Failed to load resource: " + schemaResourceName);
            }
            InputStreamReader reader = new InputStreamReader(in);
            RunScript.execute(connection, reader);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void before() {
        try {
            if (refCount.get() == 0) {
                server.start();
            }
        }
        finally {
            refCount.getAndIncrement();
        }
    }

    @Override
    protected void after() {
        refCount.getAndDecrement();
        if (refCount.get() == 0) {
            server.stop();
        }
    }

}
