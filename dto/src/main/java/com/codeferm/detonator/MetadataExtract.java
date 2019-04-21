/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import com.github.mnadeem.TableNameParser;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.sql.DataSource;

/**
 * Extract database metadata.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class MetadataExtract {

    /**
     * Default constructor.
     */
    public MetadataExtract() {
    }

    /**
     * Null safe toUpperCase.
     *
     * @param str String to upper case.
     * @return Upper case String or null;
     */
    public String toUpperCase(final String str) {
        String retStr = null;
        if (str != null) {
            retStr = str.toUpperCase(Locale.ENGLISH);
        }
        return retStr;
    }

    /**
     * Null safe toLowerCase.
     *
     * @param str String to upper case.
     * @return Upper case String or null;
     */
    public String toLowerCase(final String str) {
        String retStr = null;
        if (str != null) {
            retStr = str.toLowerCase(Locale.ENGLISH);
        }
        return retStr;
    }

    /**
     * Convert underscore separated string to camelCase.
     *
     * @param source Input string.
     * @return camelCase String.
     */
    public String toCamelCase(final String source) {
        final var sb = new StringBuffer();
        for (final var s : source.split("_")) {
            sb.append(Character.toUpperCase(s.charAt(0)));
            sb.append(s.substring(1, s.length()).toLowerCase());
        }
        return sb.toString();
    }

    /**
     * Use ResultSetMetaData DTOs to get Set of table names.
     *
     * @param sql SQL statement to parse.
     * @return Table names as Set.
     */
    public List<String> uniqueTableNames(final String sql) {
        return new TableNameParser(sql).tables().stream().collect(Collectors.toList());
    }

    /**
     * Return a Map of ResultSetMetaData DTOs keyed by column name. Extra fields have been added to make it easier to convert to a
     * DTO class.
     *
     * @param dataSource DataSoure to run queries against.
     * @param sql SQL statement used to get metadata.
     * @return Result set metadata.
     */
    public Map<String, RsmdDto> getResultSetMetaData(final DataSource dataSource, final String sql) {
        final Map<String, RsmdDto> map = new TreeMap<>();
        try (final Connection connection = dataSource.getConnection()) {
            final ResultSet resultSet;
            try (final Statement statement = connection.createStatement()) {
                resultSet = statement.executeQuery(sql);
                final ResultSetMetaData rsmd = resultSet.getMetaData();
                final int cols = rsmd.getColumnCount();
                // Map ResultSetMetaData to DTO
                for (int col = 1; col <= cols; col++) {
                    final RsmdDto dto = new RsmdDto();
                    dto.setAutoIncrement(rsmd.isAutoIncrement(col));
                    dto.setCaseSensitive(rsmd.isCaseSensitive(col));
                    dto.setCatalogName(rsmd.getCatalogName(col));
                    dto.setColumnClassName(rsmd.getColumnClassName(col));
                    dto.setColumnDisplaySize(rsmd.getColumnDisplaySize(col));
                    dto.setColumnLabel(rsmd.getColumnLabel(col));
                    dto.setColumnName(rsmd.getColumnName(col));
                    dto.setColumnType(rsmd.getColumnType(col));
                    dto.setColumnTypeName(rsmd.getColumnTypeName(col));
                    dto.setCurrency(rsmd.isCurrency(col));
                    dto.setDefinitelyWritable(rsmd.isDefinitelyWritable(col));
                    dto.setNullable(rsmd.isNullable(col));
                    dto.setPrecision(rsmd.getPrecision(col));
                    dto.setReadOnly(rsmd.isReadOnly(col));
                    dto.setScale(rsmd.getScale(col));
                    dto.setSchemaName(rsmd.getSchemaName(col));
                    dto.setSearchable(rsmd.isSearchable(col));
                    dto.setSigned(rsmd.isSigned(col));
                    dto.setTableName(rsmd.getTableName(col));
                    dto.setWritable(rsmd.isWritable(col));
                    final var camelCase = toCamelCase(rsmd.getColumnName(col));
                    dto.setMethodName(camelCase);
                    // Set first character to lower case
                    dto.setVarName(camelCase.substring(0, 1).toLowerCase(Locale.ENGLISH) + camelCase.substring(1));
                    // Split by period
                    final var array = rsmd.getColumnClassName(col).split("\\.");
                    // Save only the class without the package
                    dto.setVarType(array[array.length - 1]);
                    map.put(dto.getColumnName(), dto);
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            throw new RuntimeException(String.format("getResultSetMetaData: sql=%s", sql), e);
        }
        return map;
    }

    /**
     * Return Map of PK fields. Key determines position for composite keys.
     *
     * @param dataSource DataSoure to run queries against.
     * @param tableName Name of table.
     * @return Map of order and column names.
     */
    public Map<Integer, String> getPrimaryKey(final DataSource dataSource, String tableName) {
        final Map<Integer, String> map = new TreeMap<>();
        try (final Connection connection = dataSource.getConnection()) {
            // Table name must be upper case
            try (final ResultSet columns = connection.getMetaData().
                    getPrimaryKeys(null, null, tableName.toUpperCase(Locale.ENGLISH))) {
                while (columns.next()) {
                    map.putIfAbsent(columns.getInt("KEY_SEQ"), columns.getString("COLUMN_NAME").toUpperCase(Locale.ENGLISH));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("getPrimaryKey: tableName=%s", tableName), e);
        }
        return map;
    }

    /**
     * Get list of table and/or view names. Handles mixed, upper and lower case identifiers.
     *
     * @param dataSource DataSoure to run queries against.
     * @param catalog A catalog name must match the catalog name as it is stored in the database; "" retrieves those without a
     * catalog; null means that the catalog name should not be used to narrow the search.
     * @param schemaPattern A schema name pattern must match the schema name as it is stored in the database; "" retrieves those
     * without a schema; null means that the schema name should not be used to narrow the search.
     * @param tableNamePattern A table name pattern must match the table name as it is stored in the database.
     * @param types A list of table types, which must be from the list of table types returned from getTableTypes(),to include; null
     * returns all types.
     * @param isQuoted Use quotes identifiers?
     * @return List of table and/or view names.
     */
    public List<String> getTableNames(final DataSource dataSource, final String catalog, final String schemaPattern,
            final String tableNamePattern, final String[] types, final boolean isQuoted) {
        final List<String> list = new ArrayList<>();
        try (final Connection connection = dataSource.getConnection()) {
            final DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet resultSet;
            if (isQuoted && databaseMetaData.storesMixedCaseQuotedIdentifiers()) {
                resultSet = databaseMetaData.getTables(catalog, schemaPattern, tableNamePattern, types);
            } else if ((isQuoted && databaseMetaData.storesUpperCaseQuotedIdentifiers()) || (!isQuoted && databaseMetaData.
                    storesUpperCaseIdentifiers())) {
                resultSet = databaseMetaData.getTables(toUpperCase(catalog), toUpperCase(schemaPattern), tableNamePattern.
                        toLowerCase(Locale.ENGLISH), types);
            } else if ((isQuoted && databaseMetaData.storesLowerCaseQuotedIdentifiers()) || (!isQuoted && databaseMetaData.
                    storesLowerCaseIdentifiers())) {
                resultSet = databaseMetaData.getTables(toLowerCase(catalog), toLowerCase(schemaPattern), tableNamePattern.
                        toLowerCase(Locale.ENGLISH), types);
            } else {
                resultSet = databaseMetaData.getTables(catalog, schemaPattern, tableNamePattern, types);
            }
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                list.add(tableName.toUpperCase(Locale.ENGLISH));
            }
            resultSet.close();
        } catch (SQLException e) {
            throw new RuntimeException(String.format("getPrimaryKey: tableNamePattern=%s", tableNamePattern), e);
        }
        return list;
    }
}
