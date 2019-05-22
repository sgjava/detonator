/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.util.List;
import java.util.Map;

/**
 * Database DAO interface that simplifies common CRUD operations. Implementations should handle bean mapping and converting field
 * names with underscores to camelCase.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public interface DbDao {

    /**
     * Used when no parameters are passed.
     */
    Object[] NO_PARAMS = new Object[]{};

    /**
     * Return parameterized query results as list of beans.
     *
     * @param <T> Type of object that the handler returns.
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @param clazz Class to map results to.
     * @return List of T typed objects.
     */
    <T> List<T> selectList(final String sql, final Object[] params, final Class clazz);

    /**
     * Return query results as list of beans.
     *
     * @param <T> Type of object that the handler returns.
     * @param sql SQL statement to execute.
     * @param clazz Class to map results to.
     * @return List of T typed objects.
     */
    default <T> List<T> selectList(final String sql, final Class clazz) {
        return selectList(sql, NO_PARAMS, clazz);
    }

    /**
     * Return parameterized query results as a single bean.
     *
     * @param <T> Type of object that the handler returns.
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @param clazz Class to map results to.
     * @return T typed object.
     */
    default <T> T select(final String sql, final Object[] params, final Class clazz) {
        final List<T> list = selectList(sql, params, clazz);
        T object = null;
        if (!list.isEmpty()) {
            // Get first item in List
            object = list.get(0);
        }
        return object;
    }

    /**
     * Return query results as a single bean.
     *
     * @param <T> Type of object that the handler returns.
     * @param sql SQL statement to execute.
     * @param clazz Class to map results to.
     * @return T typed object.
     */
    default <T> T select(final String sql, final Class clazz) {
        final List<T> list = selectList(sql, NO_PARAMS, clazz);
        T object = null;
        if (!list.isEmpty()) {
            // Get first item in List
            object = list.get(0);
        }
        return object;
    }

    /**
     * Return parameterized query results as list of maps.
     *
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @return List of Maps containing field name/value pair.
     */
    List<Map<String, Object>> selectList(final String sql, final Object[] params);

    /**
     * Return query results as list of maps.
     *
     * @param sql SQL statement to execute.
     * @return List of Maps containing field name/value pair.
     */
    default List<Map<String, Object>> selectList(final String sql) {
        return selectList(sql, NO_PARAMS);
    }

    /**
     * Return parameterized query results as a single Map.
     *
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @return List of Maps containing field name/value pair.
     */
    default Map<String, Object> select(final String sql, final Object[] params) {
        final List<Map<String, Object>> list = selectList(sql, params);
        Map<String, Object> map = null;
        if (!list.isEmpty()) {
            // Get first item in List
            map = list.get(0);
        }
        return map;
    }

    /**
     * Return query results as a single Map.
     *
     * @param sql SQL statement to execute.
     * @return List of Maps containing field name/value pair.
     */
    default Map<String, Object> select(final String sql) {
        final List<Map<String, Object>> list = selectList(sql, NO_PARAMS);
        Map<String, Object> map = null;
        if (!list.isEmpty()) {
            // Get first item in List
            map = list.get(0);
        }
        return map;
    }

    /**
     * Return parameterized query results as a single typed Object.
     *
     * @param <T> Type of object that the handler returns.
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @param fieldName Name of field to return.
     * @return Object by field name.
     */
    default <T> T select(final String sql, final Object[] params, final String fieldName) {
        T object = null;
        Map<String, Object> map = select(sql, params);
        if (map != null) {
            // Get item in Map by field name
            object = (T) map.get(fieldName);
        }
        return object;
    }

    /**
     * Return query results as a single typed Object.
     *
     * @param <T> Type of object that the handler returns.
     * @param sql SQL statement to execute.
     * @param fieldName Name of field to return.
     * @return Object by field name.
     */
    default <T> T select(final String sql, final String fieldName) {
        T object = null;
        Map<String, Object> map = select(sql, NO_PARAMS);
        if (map != null) {
            // Get item in Map by field name
            object = (T) map.get(fieldName);
        }
        return object;
    }

    /**
     * Executes parameterized INSERT, UPDATE, or DELETE SQL statement.
     *
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @return Number of rows updated.
     */
    int update(final String sql, final Object[] params);

    /**
     * Executes INSERT, UPDATE, or DELETE SQL statement.
     *
     * @param sql SQL statement to execute.
     * @return Number of rows updated.
     */
    default int update(final String sql) {
        return update(sql, NO_PARAMS);
    }

    /**
     * Executes the given INSERT statement with parameter array and returns auto generate key. JDBC driver needs to support
     * RETURN_GENERATED_KEYS. {@code Connection} is closed automatically.
     *
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @param keyNames Key columns to return.
     * @return Field name/value pairs of keys.
     */
    Map<String, Object> updateReturnKeys(final String sql, final Object[] params, final String[] keyNames);

    /**
     * Executes INSERT statement and returns auto generated keys. JDBC driver needs to support RETURN_GENERATED_KEYS.
     *
     * @param sql SQL statement to execute.
     * @param keyNames Key columns to return.
     * @return Field name/value pairs of keys.
     */
    default Map<String, Object> updateReturnKeys(final String sql, final String[] keyNames) {
        return updateReturnKeys(sql, NO_PARAMS, keyNames);
    }

    /**
     * Executes INSERT statement and returns auto generated key by name. JDBC driver needs to support RETURN_GENERATED_KEYS.
     *
     * @param sql SQL statement to execute.
     * @param keyName Key name to return as int.
     * @return key value of key.
     */
    default int updateReturnKey(final String sql, final String keyName) {
        return Integer.parseInt(updateReturnKeys(sql, NO_PARAMS, new String[]{keyName}).get(keyName).toString());
    }

    /**
     * Executes parameterized INSERT statement and returns auto generated key by name. JDBC driver needs to support
     * RETURN_GENERATED_KEYS.
     *
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @param keyName Key name to return as int.
     * @return key value of key.
     */
    default int updateReturnKey(final String sql, final Object[] params, final String keyName) {
        return Integer.parseInt(updateReturnKeys(sql, params, new String[]{keyName}).get(keyName).toString());
    }

    /**
     * Executes INSERT, UPDATE or DELETE SQL statement with batch parameters.
     *
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @return Number of rows updated array.
     */
    int[] batch(final String sql, final Object[][] params);
}
