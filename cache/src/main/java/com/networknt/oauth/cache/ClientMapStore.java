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
public class ClientMapStore implements MapStore<String, Map<String, Object>> {
    static Logger logger = LoggerFactory.getLogger(ClientMapStore.class);
    static DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    static String insert = "INSERT INTO clients (client_id, client_secret, client_type, client_name, client_desc, scope, redirect_url, owner_id, create_dt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    static String delete = "DELETE FROM clients WHERE client_id = ?";
    static String select = "SELECT * FROM clients WHERE client_id = ?";
    static String update = "UPDATE clients SET client_type=?, client_name=?, client_desc=?, scope=?, redirect_url=?, owner_id=?, update_dt=? WHERE client_id=?";

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

    public synchronized void store(String key, Map<String, Object> value) {
        if(logger.isDebugEnabled()) logger.debug("Store:"  + key);
        if(load(key) == null) {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(insert)) {
                stmt.setString(1, (String)value.get("clientId"));
                stmt.setString(2, (String)value.get("clientSecret"));
                stmt.setString(3, (String)value.get("clientType"));
                stmt.setString(4, (String)value.get("clientName"));
                stmt.setString(5, (String)value.get("clientDesc"));
                stmt.setString(6, (String)value.get("scope"));
                stmt.setString(7, (String)value.get("redirectUrl"));
                stmt.setString(8, (String)value.get("ownerId"));
                stmt.setDate(9, (Date)value.get("createDt"));
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        } else {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(update)) {
                stmt.setString(1, (String)value.get("clientType"));
                stmt.setString(2, (String)value.get("clientName"));
                stmt.setString(3, (String)value.get("clientDesc"));
                stmt.setString(4, (String)value.get("scope"));
                stmt.setString(5, (String)value.get("redirectUrl"));
                stmt.setString(6, (String)value.get("ownerId"));
                stmt.setDate(7, new Date(System.currentTimeMillis()));
                stmt.setString(8, (String)value.get("clientId"));
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void storeAll(Map<String, Map<String, Object>> map) {
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet())
            store(entry.getKey(), entry.getValue());
    }

    public synchronized void deleteAll(Collection<String> keys) {
        for (String key : keys) delete(key);
    }

    public synchronized Map<String, Object> load(String key) {
        if(logger.isDebugEnabled()) logger.debug("Load:"  + key);
        Map<String, Object> result = null;
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result = new HashMap<>();
                    result.put("clientId", key);
                    result.put("clientSecret", rs.getString("client_secret"));
                    result.put("clientType", rs.getString("client_type"));
                    result.put("clientName", rs.getString("client_name"));
                    result.put("clientDesc", rs.getString("client_desc"));
                    result.put("scope", rs.getString("scope"));
                    result.put("redirectUrl", rs.getString("redirect_url"));
                    result.put("authenticateClass", rs.getString("authenticate_class"));
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

    public synchronized Map<String, Map<String, Object>> loadAll(Collection<String> keys) {
        Map<String, Map<String, Object>> result = new HashMap();
        for (String key : keys) result.put(key, load(key));
        return result;
    }

    public Iterable<String> loadAllKeys() {
        return null;
    }

}
