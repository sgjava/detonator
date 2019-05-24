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

    static final int LONG_PRECISION = String.valueOf(Long.MAX_VALUE).length();
    static final int INTEGER_PRECISION = String.valueOf(Integer.MAX_VALUE).length();
    static final int SHORT_PRECISION = String.valueOf(Short.MAX_VALUE).length();
    static final int BYTE_PRECISION = String.valueOf(Byte.MAX_VALUE).length();

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
            retStr = str.toUpperCase(Locale.US);
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
            retStr = str.toLowerCase(Locale.US);
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
     * Parse table names out of SQL.
     *
     * @param sql SQL statement to parse.
     * @return Table names as List.
     */
    public List<String> uniqueTableNames(final String sql) {
        return new TableNameParser(sql).tables().stream().collect(Collectors.toList());
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
        try (Connection connection = dataSource.getConnection()) {
            // Table name must be upper case
            try (ResultSet columns = connection.getMetaData().
                    getPrimaryKeys(null, null, tableName.toUpperCase(Locale.US))) {
                while (columns.next()) {
                    map.put(columns.getInt("KEY_SEQ"), columns.getString("COLUMN_NAME").toUpperCase(Locale.US));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("getPrimaryKey: tableName=%s", tableName), e);
        }
        return map;
    }

    /**
     * Map Java types to optimize BigDecimal with scale 0.
     *
     * @param rsmdDto Metadata DTO.
     * @return Type String.
     */
    public String mapType(final RsmdDto rsmdDto) {
        var type = rsmdDto.getColumnClassName();
        // Handle BigDecimal mapping.
        if (type.equals("java.math.BigDecimal") && rsmdDto.getScale() == 0) {
            final var precision = rsmdDto.getPrecision();
            if (precision < BYTE_PRECISION) {
                type = "java.lang.Byte";
            } else if (precision < SHORT_PRECISION) {
                type = "java.lang.Short";
            } else if (precision < INTEGER_PRECISION) {
                type = "java.lang.Integer";
            } else if (precision < LONG_PRECISION) {
                type = "java.lang.Long";
            } else {
                type = "java.math.BigInteger";
            }
            // Oracle JDBC driver returns -127 for scale of a plain NUMBER, so we map to a Long
            // TODO: Revisit and see if some smarter mapping can be done
        } else if (rsmdDto.getScale() == -127) {
            type = "java.lang.Long";
            // Always map DATE to java.sql.Date. Oracle returns a java.sql.Timestamp, so JDBC driver must handle conversion.
        } else if (rsmdDto.getColumnTypeName().toUpperCase(Locale.US).equals("DATE")) {
            type = "java.sql.Date";
        }
        return type;
    }

    /**
     * Return a Map of ResultSetMetaData DTOs keyed by column name. Extra fields have been added to make it easier to convert to a
     * DTO class.
     *
     * @param dataSource DataSoure to run queries against.
     * @param sql SQL statement used to get metadata.
     * @param mapTypes Map Java types to optimize.
     * @return Result set metadata.
     */
    public Map<String, RsmdDto> getResultSetMetaData(final DataSource dataSource, final String sql, final boolean mapTypes) {
        final Map<String, RsmdDto> map = new TreeMap<>();
        try (Connection connection = dataSource.getConnection()) {
            final ResultSet resultSet;
            try (Statement statement = connection.createStatement()) {
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
                    dto.setColumnName(rsmd.getColumnName(col).toUpperCase());
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
                    dto.setVarName(camelCase.substring(0, 1).toLowerCase(Locale.US) + camelCase.substring(1));
                    // Map Java types?
                    if (mapTypes) {
                        dto.setColumnClassName(mapType(dto));
                    }
                    // Split by period
                    final var array = dto.getColumnClassName().split("\\.");
                    // Save only the class without the package
                    dto.setVarType(array[array.length - 1]);
                    // Make sure Map key is always upper case, so not dependent on metadata result
                    map.put(dto.getColumnName().toUpperCase(Locale.US), dto);
                }
            }
            resultSet.close();
            // Get table names from SQL
            var tables = uniqueTableNames(sql);
            // Get PK information only for single table SQL
            if (tables.size() == 1) {
                final var pkMap = getPrimaryKey(dataSource, tables.get(0));
                // Set PK sequence in DTO
                pkMap.entrySet().forEach((final                         var entry) -> {
                    map.get(entry.getValue()).setKeySeq(entry.getKey());
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException(String.format("getResultSetMetaData: sql=%s", sql), e);
        }
        return map;
    }

    /**
     * Override key columns based on column name. Can be used to give tables and composites the ability to generate a key. List
     * order determines key sequence.
     *
     * @param map Map of ResultSetMetaData DTOs
     * @param list List of column names.
     */
    public void overridePrimaryKey(final Map<String, RsmdDto> map, final List<String> list) {
        // Set all key sequences to null
        map.entrySet().forEach(entry -> {
            entry.getValue().setKeySeq(null);
        });
        // Key sequence starts with 1.
        int i = 1;
        // Set new key sequences and skip missing column names
        for (final var columnName : list) {
            final var dto = map.get(columnName);
            if (dto != null) {
                dto.setKeySeq(i++);
            }
        }
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
     * @param isQuoted Use quoted identifiers?
     * @return List of table and/or view names.
     */
    public List<String> getTableNames(final DataSource dataSource, final String catalog, final String schemaPattern,
            final String tableNamePattern, final String[] types, final boolean isQuoted) {
        final List<String> list = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            final DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet resultSet;
            if (isQuoted && databaseMetaData.storesMixedCaseQuotedIdentifiers()) {
                resultSet = databaseMetaData.getTables(catalog, schemaPattern, tableNamePattern, types);
            } else if (isQuoted && databaseMetaData.storesUpperCaseQuotedIdentifiers() || !isQuoted && databaseMetaData.
                    storesUpperCaseIdentifiers()) {
                resultSet = databaseMetaData.getTables(toUpperCase(catalog), toUpperCase(schemaPattern), tableNamePattern.
                        toLowerCase(Locale.US), types);
            } else if (isQuoted && databaseMetaData.storesLowerCaseQuotedIdentifiers() || !isQuoted && databaseMetaData.
                    storesLowerCaseIdentifiers()) {
                resultSet = databaseMetaData.getTables(toLowerCase(catalog), toLowerCase(schemaPattern), tableNamePattern.
                        toLowerCase(Locale.US), types);
            } else {
                resultSet = databaseMetaData.getTables(catalog, schemaPattern, tableNamePattern, types);
            }
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                list.add(tableName.toUpperCase(Locale.US));
            }
            resultSet.close();
        } catch (SQLException e) {
            throw new RuntimeException(String.format("getTableNames: tableNamePattern=%s", tableNamePattern), e);
        }
        return list;
    }
}
