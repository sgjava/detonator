/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.plugins;

import com.codeferm.detonator.MakeDto;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;

import java.util.Map;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Generate code for Mojos.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class GenCode {

    /**
     * Database driver.
     */
    private String dbDriver;
    /**
     * Database user.
     */
    private String dbUser;
    /**
     * Database password.
     */
    private String dbPassword;
    /**
     * Database URL.
     */
    private String dbUrl;
    /**
     * DataSource pool size.
     */
    private int dbPoolSize;
    /**
     * Map Java types.
     */
    private boolean mapTypes;
    /**
     * Location of generated sources dir.
     */
    private String genSrcDir;
    /**
     * Location of generated resources dir.
     */
    private String genResDir;
    /**
     * FreeMarker templates path.
     */
    private String templatesDir;
    /**
     * DTO template.
     */
    private String dtoTemplate;
    /**
     * ID template.
     */
    private String idTemplate;
    /**
     * ID template.
     */
    private String sqlTemplate;
    /**
     * Map of class name (key) and SQL (value).
     */
    private Map<String, String> sqlMap;
    /**
     * Package name to use for generated classes.
     */
    private String packageName;

    /* Default constructor */
    public GenCode() {
    }

    public String getDbDriver() {
        return dbDriver;
    }

    public void setDbDriver(final String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(final String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(final String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(final String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public int getDbPoolSize() {
        return dbPoolSize;
    }

    public void setDbPoolSize(final int dbPoolSize) {
        this.dbPoolSize = dbPoolSize;
    }

    public boolean isMapTypes() {
        return mapTypes;
    }

    public void setMapTypes(final boolean mapTypes) {
        this.mapTypes = mapTypes;
    }

    public String getGenSrcDir() {
        return genSrcDir;
    }

    public void setGenSrcDir(final String genSrcDir) {
        this.genSrcDir = genSrcDir;
    }

    public String getGenResDir() {
        return genResDir;
    }

    public void setGenResDir(final String genResDir) {
        this.genResDir = genResDir;
    }

    public String getTemplatesDir() {
        return templatesDir;
    }

    public void setTemplatesDir(final String templatesDir) {
        this.templatesDir = templatesDir;
    }

    public String getDtoTemplate() {
        return dtoTemplate;
    }

    public void setDtoTemplate(final String dtoTemplate) {
        this.dtoTemplate = dtoTemplate;
    }

    public String getIdTemplate() {
        return idTemplate;
    }

    public void setIdTemplate(final String idTemplate) {
        this.idTemplate = idTemplate;
    }

    public String getSqlTemplate() {
        return sqlTemplate;
    }

    public void setSqlTemplate(final String sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }

    public Map<String, String> getSqlMap() {
        return sqlMap;
    }

    public void setSqlMap(final Map<String, String> sqlMap) {
        this.sqlMap = sqlMap;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    /**
     * Generate source and property files.
     */
    public void execute() {
        // Create DBCP DataSource
        final var dataSource = new BasicDataSource();
        dataSource.setDriverClassName(dbDriver);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);
        dataSource.setUrl(dbUrl);
        dataSource.setMaxTotal(dbPoolSize);
        // Make dirs
        final var sourceDir = String.format("%s/java/%s", genSrcDir, packageName.replace('.', '/'));
        final var gsDir = new File(sourceDir);
        if (!gsDir.mkdirs()) {
            throw new RuntimeException(String.format("Failed to make directory %s", sourceDir));
        }
        final var grDir = new File(genResDir);
        if (!grDir.mkdirs()) {
            throw new RuntimeException(String.format("Failed to make directory %s", genResDir));
        }
        final var makeDto = new MakeDto(dataSource, templatesDir);
        // Generate classes based on SQL Map
        sqlMap.entrySet().forEach((var entry) -> {
            try {
                // Use FileOutputStream for SQL properties output
                try (var out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(String.format("%s/%s.properties",
                        genResDir, entry.getKey().toLowerCase(Locale.US))), false), StandardCharsets.UTF_8))) {
                    makeDto.sqlTemplate(sqlTemplate, entry.getValue(), mapTypes, out);
                }
                // Use FileOutputStream for DTO output
                try (var out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(String.format("%s/%s.java",
                        sourceDir, entry.getKey())), false), StandardCharsets.UTF_8))) {
                    makeDto.dtoTemplate(dtoTemplate, entry.getValue(), packageName, entry.getKey(), mapTypes, out);
                }
                // Use StringWriter in case ID is empty (i.e. no PK or composite SQL)
                final var out = new StringWriter();
                makeDto.idTemplate(idTemplate, entry.getValue(), packageName, String.format("%sId", entry.getKey()), mapTypes, out);
                final var idStr = out.toString();
                // Check for empty result
                if (!idStr.isEmpty()) {
                    // Use FileOutputStream for ID output
                    try (var idOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(String.format(
                            "%s/%sId.java", sourceDir, entry.getKey())), false), StandardCharsets.UTF_8))) {
                        idOut.write(idStr);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Write templates", e);
            }
        });
        // Close DataSource
        try {
            ((BasicDataSource) dataSource).close();
        } catch (SQLException e) {
            throw new RuntimeException("Close DataSource", e);
        }
    }
}
