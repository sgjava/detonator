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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.sql.DataSource;

/**
 * Make DTO from database metadata and template.
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
     * Set DataSource variable types Set.
     *
     * @param dataSource DataSoure to run queries against.
     * @param templateDir Template directory.
     */
    public MakeDto(final DataSource dataSource, final String templateDir) {
        this.dataSource = dataSource;
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
     * Normalize classes for import section of DTO.
     *
     * @param map Map of RsmdDtos keyed by column name.
     * @return Set of unique class names without java.lang.* classes.
     */
    public Set<String> getImports(final Map<String, RsmdDto> map) {
        final var classes = new TreeSet<String>();
        // Used for equals and hashCode methods
        classes.add("java.util.Objects");
        map.entrySet().stream().map((final  var entry) -> entry.getValue()).filter((value) -> (value.getColumnClassName().startsWith(
                "java.lang"))).forEachOrdered((final var value) -> {
            classes.add(value.getColumnClassName());
        });
        return classes;
    }

    /**
     * Use database metadata to generate Java DTO. Pass in the Writer required for a particular purpose.
     *
     * @param sql SQL used to generate metadata.
     * @param packageName Java package name.
     * @param className Java class name.
     * @param writer Template output.
     */
    public void dtoTemplate(final String sql, final String packageName, final String className, final Writer writer) {
        final var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        final var metadataExtract = new MetadataExtract();
        final var map = metadataExtract.getResultSetMetaData(dataSource, sql);
        // Template model
        final Map<String, Object> model = new HashMap<>();
        model.put("imports", getImports(map));
        model.put("packageName", packageName);
        model.put("now", LocalDateTime.now().format(formatter));
        // Remove new line chars, so SQL statement fits on one line in comment.
        model.put("sql", sql.replaceAll("\\R", " "));
        model.put("className", className);
        model.put("map", map);
        // Process DTO template
        try {
            final var temp = configuration.getTemplate("dto.ftl");
            temp.process(model, writer);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Use database metadata to generate SQL statements. Pass in the Writer required for a particular purpose. For composite SQL
     * (i.e. more than one table) or tables without a PK no output will be generated since that is required for DML operations.
     *
     * @param sql SQL used to generate metadata.
     * @param writer Template output.
     */
    public void sqlTemplate(final String sql, final Writer writer) {
        final var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        final var metadataExtract = new MetadataExtract();
        final var map = metadataExtract.getResultSetMetaData(dataSource, sql);
        final var tables = metadataExtract.uniqueTableNames(sql);
        // If SQL has more than on table then skip template processing
        if (tables.size() == 1) {
            final var tableName = tables.iterator().next();
            // Get PK fields
            final var pk = metadataExtract.getPrimaryKey(dataSource, tableName);
            // Template model
            final Map<String, Object> model = new HashMap<>();
            model.put("now", LocalDateTime.now().format(formatter));
            // Remove new line chars, so SQL statement fits on one line in comment.
            model.put("sql", sql.replaceAll("\\R", " "));
            model.put("table", tableName);
            model.put("pk", pk);
            model.put("map", map);
            // Process DTO template
            try {
                final var temp = configuration.getTemplate("sql.ftl");
                temp.process(model, writer);
            } catch (IOException | TemplateException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
