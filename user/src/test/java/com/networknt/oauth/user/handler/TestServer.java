package com.networknt.oauth.user.handler;

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

public class TestServer extends ExternalResource {
    static final Logger logger = LoggerFactory.getLogger(TestServer.class);

    private static volatile int refCount = 0;
    private static Server server;

    private static final TestServer instance  = new TestServer();

    public static TestServer getInstance () {
        return instance;
    }

    private TestServer() {
        DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
        try (Connection connection = ds.getConnection()) {
            // Runscript doesn't work need to execute batch here.
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

    protected void before() {
        try {
            if (refCount == 0) {
                server.start();
            }
        }
        finally {
            refCount++;
        }
    }

    protected void after() {
        refCount--;
        if (refCount == 0) {
            server.stop();
        }
    }

}
