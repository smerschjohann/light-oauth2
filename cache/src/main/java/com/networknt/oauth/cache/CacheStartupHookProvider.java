package com.networknt.oauth.cache;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.networknt.oauth.cache.model.UserDataSerializableFactory;
import com.networknt.server.StartupHookProvider;

/**
 * Created by stevehu on 2016-12-27.
 */
public class CacheStartupHookProvider implements StartupHookProvider {
    public static HazelcastInstance hz;
    @Override
    public void onStartup() {
        Config config = new Config();
        config.getNetworkConfig().setPort( 5900 )
                .setPortAutoIncrement( true );

        // data serializer factory
        config.getSerializationConfig().addDataSerializableFactory(1, new UserDataSerializableFactory());

        // service map with near cache.
        MapConfig serviceConfig = new MapConfig();
        serviceConfig.setName("services");
        NearCacheConfig serviceCacheConfig = new NearCacheConfig();
        serviceCacheConfig.setEvictionPolicy("NONE");
        serviceCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        serviceCacheConfig.setCacheLocalEntries(true); // this enables the local caching
        serviceConfig.setNearCacheConfig(serviceCacheConfig);

        serviceConfig.getMapStoreConfig()
                .setEnabled(true)
                .setClassName("com.networknt.oauth.cache.ServiceMapStore");

        config.addMapConfig(serviceConfig);

        // client map with near cache.
        MapConfig clientConfig = new MapConfig();
        clientConfig.setName("clients");
        NearCacheConfig clientCacheConfig = new NearCacheConfig();
        clientCacheConfig.setEvictionPolicy("NONE");
        clientCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        clientCacheConfig.setCacheLocalEntries(true); // this enables the local caching
        clientConfig.setNearCacheConfig(clientCacheConfig);

        clientConfig.getMapStoreConfig()
                .setEnabled(true)
                .setClassName("com.networknt.oauth.cache.ClientMapStore");

        config.addMapConfig(clientConfig);

        // code map with near cache and evict.
        MapConfig codeConfig = new MapConfig();
        codeConfig.setName("codes");
        NearCacheConfig codeCacheConfig = new NearCacheConfig();
        codeCacheConfig.setTimeToLiveSeconds(60 * 60 * 1000); // 1 hour TTL
        codeCacheConfig.setMaxIdleSeconds(10 * 60 * 1000);    // 10 minutes max idle seconds
        codeCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        codeCacheConfig.setCacheLocalEntries(true); // this enables the local caching
        codeConfig.setNearCacheConfig(codeCacheConfig);
        config.addMapConfig(codeConfig);

        // user map distributed.
        MapConfig userConfig = new MapConfig();
        userConfig.setName("users");
        userConfig.setBackupCount(1);
        userConfig.getMapStoreConfig()
                .setEnabled(true)
                .setClassName("com.networknt.oauth.cache.UserMapStore");
        userConfig.addMapIndexConfig(new MapIndexConfig("email", true));

        config.addMapConfig(userConfig);

        hz = Hazelcast.newHazelcastInstance( config );

    }
}
