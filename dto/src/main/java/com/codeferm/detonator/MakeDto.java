/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.sql.DataSource;

/**
 * Make DTO from database metadata and templates. This class should be considered thread safe and only one instance is required.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class MakeDto {

    /**
     * DataSource.
     */
    private final DataSource dataSource;
    /**
     * FreeMarker configuration singleton.
     */
    private final Configuration configuration = new Configuration(Configuration.getVersion());
    /**
     * Map data types.
     */
    private final boolean mapTypes;

    /**
     * Set DataSource variable types Set.
     *
     * @param dataSource DataSoure to run queries against.
     * @param templateDir Template directory.
     * @param mapTypes Map Java types to optimize out BigDecimal
     */
    public MakeDto(final DataSource dataSource, final String templateDir, final boolean mapTypes) {
        this.dataSource = dataSource;
        this.mapTypes = mapTypes;
        try {
            configuration.setDirectoryForTemplateLoading(new File(templateDir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
    }

    /**
     * Normalize classes.
     *
     * @param map Map of RsmdDtos keyed by column name.
     * @param pkOnly Include only PK fields.
     * @return Set of unique class names without java.lang.* classes.
     */
    public Set<String> getClasses(final Map<String, RsmdDto> map, final boolean pkOnly) {
        final var classes = new TreeSet<String>();
        map.entrySet().stream().map(entry -> entry.getValue()).filter(value -> !value.getColumnClassName().
                startsWith("java.lang")).forEachOrdered((final         var value) -> {
            // Only include PK columns?
            if (pkOnly) {
                if (value.getKeySeq() != null) {
                    classes.add(value.getColumnClassName());
                }
            } else {
                classes.add(value.getColumnClassName());
            }
        });
        return classes;
    }

    /**
     * Return Map of PK fields or empty Map if none.
     *
     * @param map Metadata Map.
     * @return PK columns Map.
     */
    public Map<String, RsmdDto> getPkMap(final Map<String, RsmdDto> map) {
        // Create new map with just PK columns
        final Map<String, RsmdDto> pkMap = new TreeMap<>();
        map.entrySet().forEach((entry) -> {
            final var value = entry.getValue();
            if (value.getKeySeq() != null) {
                pkMap.put(entry.getKey(), value);
            }
        });
        return pkMap;
    }

    /**
     * Use database metadata to generate Java DTO. Pass in the Writer required for a particular purpose.
     *
     * @param template Template to use.
     * @param sql SQL used to generate metadata.
     * @param list List of primary key column overrides.
     * @param packageName Java package name.
     * @param className Java class name.
     * @param writer Template output.
     */
    public void dtoTemplate(final String template, final String sql, final List<String> list, final String packageName,
            final String className,
            final Writer writer) {
        final var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        final var metadataExtract = new MetadataExtract();
        final var map = metadataExtract.getResultSetMetaData(dataSource, sql, mapTypes);
        // Overried primary key columns
        if (list != null) {
            metadataExtract.overridePrimaryKey(map, list);
        }
        // Template model
        final Map<String, Object> model = new HashMap<>();
        model.put("imports", getClasses(map, false));
        model.put("packageName", packageName);
        model.put("now", LocalDateTime.now().format(formatter));
        // Remove new line chars, so SQL statement fits on one line in comment.
        model.put("sql", sql.replaceAll("\\R", " "));
        model.put("className", className);
        model.put("map", map);
        model.put("pkMap", getPkMap(map));
        // Process DTO template
        try {
            final var temp = configuration.getTemplate(template);
            temp.process(model, writer);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Use database metadata to generate Java key objects. Pass in the Writer required for a particular purpose.
     *
     * @param template Template to use.
     * @param sql SQL used to generate metadata.
     * @param list List of primary key column overrides.
     * @param packageName Java package name.
     * @param className Java class name.
     * @param writer Template output.
     */
    public void keyTemplate(final String template, final String sql, final List<String> list, final String packageName,
            final String className,
            final Writer writer) {
        final var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        final var metadataExtract = new MetadataExtract();
        final var map = metadataExtract.getResultSetMetaData(dataSource, sql, mapTypes);
        // Overried primary key columns
        if (list != null) {
            metadataExtract.overridePrimaryKey(map, list);
        }
        // Map with just PK columns
        final var pkMap = getPkMap(map);
        // Map of PK columns ordered by sequence
        final var pkOrder = new TreeMap<Integer, RsmdDto>();
        pkMap.entrySet().forEach(entry -> {
            pkOrder.put(entry.getValue().getKeySeq(), entry.getValue());
        });
        // Skip generation if no PK columns
        if (!pkMap.isEmpty()) {
            // Template model
            final Map<String, Object> model = new HashMap<>();
            model.put("imports", getClasses(map, true));
            model.put("packageName", packageName);
            model.put("now", LocalDateTime.now().format(formatter));
            // Remove new line chars, so SQL statement fits on one line in comment.
            model.put("sql", sql.replaceAll("\\R", " "));
            model.put("className", className);
            model.put("map", pkMap);
            model.put("mapOrder", pkOrder);
            // Process ID template
            try {
                final var temp = configuration.getTemplate(template);
                temp.process(model, writer);
            } catch (IOException | TemplateException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Use database metadata to generate SQL statements. Pass in the Writer required for a particular purpose. For composite SQL
     * (i.e. more than one table) or tables without a PK the template will not generate DML operations.
     *
     * @param template Template to use.
     * @param sql SQL used to generate metadata.
     * @param list List of primary key column overrides.
     * @param writer Template output.
     */
    public void sqlTemplate(final String template, final String sql, final List<String> list, final Writer writer) {
        final var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        final var metadataExtract = new MetadataExtract();
        final var map = metadataExtract.getResultSetMetaData(dataSource, sql, mapTypes);
        // Overried primary key columns
        if (list != null) {
            metadataExtract.overridePrimaryKey(map, list);
        }
        final var tables = metadataExtract.uniqueTableNames(sql);
        final var tableName = tables.iterator().next();
        // Get PK fields
        final var pkMap = metadataExtract.getPrimaryKey(dataSource, tableName);
        // Sorted PK fields
        final Set<String> pkSet = new TreeSet<>();
        pkMap.entrySet().forEach((entry) -> {
            pkSet.add(entry.getValue());
        });
        // Template model
        final Map<String, Object> model = new HashMap<>();
        model.put("now", LocalDateTime.now().format(formatter));
        // Remove new line chars, so SQL statement fits on one line in comment.
        model.put("sql", sql.replaceAll("\\R", " "));
        model.put("tables", tables);
        model.put("table", tableName);
        model.put("pkSet", pkSet);
        model.put("map", map);
        // Process SQL template
        try {
            final var temp = configuration.getTemplate(template);
            temp.process(model, writer);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }
}
