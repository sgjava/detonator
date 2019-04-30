/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.plugins;

import java.util.Locale;
import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.Map;

/**
 * Goal to generate DTO, ID classes and properties based on SQL.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class BaseGenMojo extends AbstractMojo {

    /**
     * Database driver.
     */
    @Parameter(property = "dbDriver", required = true)
    private String dbDriver;
    /**
     * Database user.
     */
    @Parameter(property = "dbUser", required = true)
    private String dbUser;
    /**
     * Database password. A value of empty will be converted to empty String.
     */
    @Parameter(defaultValue = "empty", property = "dbPassword", required = true)
    private String dbPassword;
    /**
     * Database URL.
     */
    @Parameter(property = "dbUrl", required = true)
    private String dbUrl;
    /**
     * DataSource pool size.
     */
    @Parameter(property = "dbPoolSize", required = true)
    private int dbPoolSize;
    /**
     * FreeMarker templates path.
     */
    @Parameter(property = "templatesDir", required = true)
    private String templatesDir;
    /**
     * DTO template.
     */
    @Parameter(property = "dtoTemplate", required = true)
    private String dtoTemplate;
    /**
     * ID template.
     */
    @Parameter(property = "idTemplate", required = true)
    private String idTemplate;
    /**
     * ID template.
     */
    @Parameter(property = "sqlTemplate", required = true)
    private String sqlTemplate;
    /**
     * Map of class name (key) and SQL (value).
     */
    @Parameter(property = "sqlMap", required = true)
    private Map<String, String> sqlMap;
    /**
     * Package name to use for generated classes.
     */
    @Parameter(property = "packageName", required = true)
    private String packageName;
    /* Default constructor */
    public BaseGenMojo() {
    }

    /**
     * Code generator accessor.
     *
     * @return Code generator.
     */
    public GenCode getGenCode() {
        final var genCode = new GenCode();
        // Maven doesn't allow empty parameters
        if (dbPassword.toLowerCase(Locale.US).equals("empty")) {
            dbPassword = "";
        }
        genCode.setDbDriver(dbDriver);
        genCode.setDbUrl(dbUrl);
        genCode.setDbUser(dbUser);
        genCode.setDbPassword(dbPassword);
        genCode.setDbPoolSize(dbPoolSize);
        genCode.setTemplatesDir(templatesDir);
        genCode.setDtoTemplate(dtoTemplate);
        genCode.setIdTemplate(idTemplate);
        genCode.setSqlTemplate(sqlTemplate);
        genCode.setPackageName(packageName);
        genCode.setSqlMap(sqlMap);
        return genCode;
    }
}
