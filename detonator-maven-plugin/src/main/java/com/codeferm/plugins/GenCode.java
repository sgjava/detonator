/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.plugins;

import com.codeferm.detonator.MakeDto;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
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

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public int getDbPoolSize() {
        return dbPoolSize;
    }

    public void setDbPoolSize(int dbPoolSize) {
        this.dbPoolSize = dbPoolSize;
    }

    public String getGenSrcDir() {
        return genSrcDir;
    }

    public void setGenSrcDir(String genSrcDir) {
        this.genSrcDir = genSrcDir;
    }

    public String getGenResDir() {
        return genResDir;
    }

    public void setGenResDir(String genResDir) {
        this.genResDir = genResDir;
    }

    public String getTemplatesDir() {
        return templatesDir;
    }

    public void setTemplatesDir(String templatesDir) {
        this.templatesDir = templatesDir;
    }

    public String getDtoTemplate() {
        return dtoTemplate;
    }

    public void setDtoTemplate(String dtoTemplate) {
        this.dtoTemplate = dtoTemplate;
    }

    public String getIdTemplate() {
        return idTemplate;
    }

    public void setIdTemplate(String idTemplate) {
        this.idTemplate = idTemplate;
    }

    public String getSqlTemplate() {
        return sqlTemplate;
    }

    public void setSqlTemplate(String sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }

    public Map<String, String> getSqlMap() {
        return sqlMap;
    }

    public void setSqlMap(Map<String, String> sqlMap) {
        this.sqlMap = sqlMap;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
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
        gsDir.mkdirs();
        final var grDir = new File(genResDir);
        grDir.mkdirs();
        final var makeDto = new MakeDto(dataSource, templatesDir);
        // Generate classes based on SQL Map
        sqlMap.entrySet().forEach((entry) -> {
            try {
                // Use FileWriter for SQL properties output
                try (final var out = new BufferedWriter(new FileWriter(String.format("%s/%s.properties", genResDir, entry.getKey().
                        toLowerCase(Locale.US))))) {
                    makeDto.sqlTemplate(sqlTemplate, entry.getValue(), out);
                }
                // Use FileWriter for DTO output
                try (final var out = new BufferedWriter(new FileWriter(String.format("%s/%s.java", sourceDir, entry.getKey())))) {
                    makeDto.dtoTemplate(dtoTemplate, entry.getValue(), packageName, entry.getKey(), out);
                }
                // Use StringWriter in case ID is empty (i.e. no PK or composite SQL)
                final var out = new StringWriter();
                makeDto.idTemplate(idTemplate, entry.getValue(), packageName, String.format("%sId", entry.getKey()), out);
                final var idStr = out.toString();
                // Check for empty result
                if (!idStr.isEmpty()) {
                    // Use FileWriter for ID output
                    try (final var idOut = new BufferedWriter(new FileWriter(String.format("%s/%sId.java", sourceDir, entry.getKey())))) {
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
