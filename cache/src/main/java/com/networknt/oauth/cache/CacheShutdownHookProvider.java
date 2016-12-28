package com.networknt.oauth.cache;

import com.hazelcast.core.HazelcastInstance;
import com.networknt.server.ShutdownHookProvider;
import com.networknt.server.StartupHookProvider;

/**
 * Created by stevehu on 2016-12-27.
 */
public class CacheShutdownHookProvider implements ShutdownHookProvider {
    @Override
    public void onShutdown() {
        CacheStartupHookProvider.hz.shutdown();
    }
}
