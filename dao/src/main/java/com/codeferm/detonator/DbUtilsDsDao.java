/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.GenerousBeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

/**
 * DbUtils DataSource implementation of Dao interface. This class should be considered thread safe since QueryRunner is thread safe.
 * Do not use connection based transactions since there's no guarantee the same connection is used for each method. You must use JTA
 * based transactions for DataSources.
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class DbUtilsDsDao implements DbDao {

    /**
     * QueryRunner is thread safe.
     */
    private transient QueryRunner queryRunner = null;

    /**
     * Construct new {@code QueryRunner} with {@code DataSource}.
     *
     * @param dataSource Database data source.
     */
    public DbUtilsDsDao(final DataSource dataSource) {
        super();
        queryRunner = new QueryRunner(dataSource);
    }

    /**
     * Return query results as list of beans. {@code Connection} is closed automatically.
     *
     * @param <T> Type of object that the handler returns.
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @param clazz {@code Class} to map results to.
     * @return {@code List} of {@code <T>} typed objects.
     */
    @Override
    public final <T> List<T> selectList(final String sql, final Object[] params, final Class clazz) {
        List<T> list = null;
        try {
            list = (List<T>) queryRunner.query(sql, new BeanListHandler(clazz, new BasicRowProcessor(new GenerousBeanProcessor())),
                    params);
        } catch (SQLException e) {
            throw new RuntimeException(String.format("selectBeanList: sql=%s, params=%s", sql, Arrays.asList(params)), e);
        }
        return list;
    }

    /**
     * Return query results as list of Maps. {@code Connection} is closed automatically.
     *
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @return {@code List} of Maps containing field name/value pair.
     */
    @Override
    public final List<Map<String, Object>> selectList(final String sql, final Object[] params) {
        List<Map<String, Object>> list = null;
        try {
            list = queryRunner.query(sql, new MapListHandler(), params);
        } catch (SQLException e) {
            throw new RuntimeException(String.format("selectMapList: sql=%s, params=%s", sql, Arrays.asList(params)), e);
        }
        return list;
    }

    /**
     * Executes the given INSERT, UPDATE, or DELETE SQL statement with parameter array. {@code Connection} is closed automatically.
     *
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @return Number of rows updated.
     */
    @Override
    public final int update(final String sql, final Object[] params) {
        int rows = -1;
        try {
            rows = queryRunner.update(sql, params);
        } catch (SQLException e) {
            throw new RuntimeException(String.format("update: sql=%s, params=%s", sql, Arrays.asList(params)), e);
        }
        return rows;
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
    @Override
    public final Map<String, Object> updateReturnKeys(final String sql, final Object[] params, final String[] keyNames) {
        Map<String, Object> keys = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            // Get Connection from QueryRunner DataSource
            connection = queryRunner.getDataSource().getConnection();
            // Oracle will return ROW_ID if not specified
            if (keyNames != null) {
                preparedStatement = connection.prepareStatement(sql, keyNames);
            } else {
                // H2 is fine without specifiying key columns
                preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            }
            // Fill parameters
            for (var i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            preparedStatement.executeUpdate();
            // Get keys as ResultSet
            resultSet = preparedStatement.getGeneratedKeys();
            // Get generated keys as Object array
            keys = new MapHandler().handle(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(String.format("updateReturnKeys: sql=%s, params=%s", sql, Arrays.asList(params)), e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(preparedStatement);
            DbUtils.closeQuietly(connection);
        }
        return keys;
    }

    /**
     * Executes the given INSERT, UPDATE, or DELETE SQL statement with array of parameter arrays.
     *
     * @param sql SQL statement to execute.
     * @param params Initialize the PreparedStatement's IN parameters.
     * @return Number of rows updated array.
     */
    @Override
    public final int[] batch(final String sql, final Object[][] params) {
        int[] rows = null;
        try {
            rows = queryRunner.batch(sql, params);
        } catch (SQLException e) {
            throw new RuntimeException(String.format("batch: sql=%s, params=%s", sql, Arrays.asList(params)), e);
        }
        return rows;
    }
}
