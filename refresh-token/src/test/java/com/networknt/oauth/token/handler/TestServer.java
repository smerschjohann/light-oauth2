package com.networknt.oauth.token.handler;

import com.networknt.server.Server;
import org.junit.rules.ExternalResource;
import java.util.concurrent.atomic.AtomicInteger;

public class TestServer extends ExternalResource {

    private static final AtomicInteger refCount = new AtomicInteger(0);

    private static final TestServer instance  = new TestServer();

    static TestServer getInstance() {
        return instance;
    }

    private TestServer() {

    }

    @Override
    protected void before() {
        try {
            if (refCount.get() == 0) {
                Server.start();
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
            Server.stop();
        }
    }
}
