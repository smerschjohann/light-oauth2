package com.networknt.oauth.cache;

import com.hazelcast.core.MapStore;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stevehu on 2016-12-27.
 */
public class ServiceMapStore implements MapStore<String, Map<String, Object>> {
    static final Logger logger = LoggerFactory.getLogger(ServiceMapStore.class);
    static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static final String insert = "INSERT INTO services (service_id, service_type, service_name, service_desc, scope, owner_id, create_dt) VALUES (?, ?, ?, ?, ?, ?, ?)";
    static final String delete = "DELETE FROM services WHERE service_id = ?";
    static final String select = "SELECT * FROM services WHERE service_id = ?";
    static final String update = "UPDATE services SET service_type = ?, service_name=?, service_desc=?, scope=?, owner_id=?, update_dt=? WHERE service_id=?";
    @Override
    public synchronized void delete(String key) {
        if(logger.isDebugEnabled()) logger.debug("Delete:" + key);
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(delete)) {
            stmt.setString(1, key);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
    }
    @Override
    public synchronized void store(String key, Map<String, Object> value) {
        if(logger.isDebugEnabled()) logger.debug("Store:"  + key);
        if(load(key) == null) {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(insert)) {
                stmt.setString(1, (String)value.get("serviceId"));
                stmt.setString(2, (String)value.get("serviceType"));
                stmt.setString(3, (String)value.get("serviceName"));
                stmt.setString(4, (String)value.get("serviceDesc"));
                stmt.setString(5, (String)value.get("scope"));
                stmt.setString(6, (String)value.get("ownerId"));
                stmt.setDate(7, new Date(System.currentTimeMillis()));
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        } else {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(update)) {
                stmt.setString(1, (String)value.get("serviceType"));
                stmt.setString(2, (String)value.get("serviceName"));
                stmt.setString(3, (String)value.get("serviceDesc"));
                stmt.setString(4, (String)value.get("scope"));
                stmt.setString(5, (String)value.get("ownerId"));
                stmt.setDate(6, new Date(System.currentTimeMillis()));
                stmt.setString(7, (String)value.get("serviceId"));
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public synchronized void storeAll(Map<String, Map<String, Object>> map) {
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet())
            store(entry.getKey(), entry.getValue());
    }
    @Override
    public synchronized void deleteAll(Collection<String> keys) {
        keys.forEach(this::delete);
    }
    @Override
    public synchronized Map<String, Object> load(String key) {
        if(logger.isDebugEnabled()) logger.debug("Load:"  + key);
        Map<String, Object> result = null;
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result = new HashMap<>();
                    result.put("serviceId", key);
                    result.put("serviceType", rs.getString("service_type"));
                    result.put("serviceName", rs.getString("service_name"));
                    result.put("serviceDesc", rs.getString("service_desc"));
                    result.put("scope", rs.getString("scope"));
                    result.put("ownerId", rs.getString("owner_id"));
                    result.put("createDt", rs.getDate("create_dt"));
                    result.put("updateDt", rs.getDate("update_dt"));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        return result;
    }
    @Override
    public synchronized Map<String, Map<String, Object>> loadAll(Collection<String> keys) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (String key : keys) result.put(key, load(key));
        return result;
    }
    @Override
    public Iterable<String> loadAllKeys() {
        return null;
    }

}
