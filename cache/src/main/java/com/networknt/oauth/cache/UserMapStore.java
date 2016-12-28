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
public class UserMapStore implements MapStore<String, Map<String, Object>> {
    static Logger logger = LoggerFactory.getLogger(ServiceMapStore.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String insert = "INSERT INTO users (user_id, user_type, first_name, last_name, email, password, create_dt) VALUES (?, ?, ?, ?, ?, ?, ?)";
    static String delete = "DELETE FROM users WHERE user_id = ?";
    static String select = "SELECT * FROM users WHERE user_id = ?";
    static String update = "UPDATE users SET user_type=?, first_name=?, last_name=?, email=?, password=?, update_dt=? WHERE user_id = ?";
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
                stmt.setString(1, (String)value.get("userId"));
                stmt.setString(2, (String)value.get("userType"));
                stmt.setString(3, (String)value.get("firstName"));
                stmt.setString(4, (String)value.get("lastName"));
                stmt.setString(5, (String)value.get("email"));
                stmt.setString(6, (String)value.get("password"));
                stmt.setDate(7, new Date(System.currentTimeMillis()));
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        } else {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(update)) {
                stmt.setString(1, (String)value.get("userType"));
                stmt.setString(2, (String)value.get("firstName"));
                stmt.setString(3, (String)value.get("lastName"));
                stmt.setString(4, (String)value.get("email"));
                stmt.setString(5, (String)value.get("password"));
                stmt.setDate(6, new Date(System.currentTimeMillis()));
                stmt.setString(7, (String)value.get("userId"));
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
        for (String key : keys) delete(key);
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
                    result.put("userId", key);
                    result.put("userType", rs.getString("user_type"));
                    result.put("firstName", rs.getString("first_name"));
                    result.put("lastName", rs.getString("last_name"));
                    result.put("email", rs.getString("email"));
                    result.put("password", rs.getString("password"));
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
        Map<String, Map<String, Object>> result = new HashMap();
        for (String key : keys) result.put(key, load(key));
        return result;
    }
    @Override
    public Iterable<String> loadAllKeys() {
        return null;
    }
}
