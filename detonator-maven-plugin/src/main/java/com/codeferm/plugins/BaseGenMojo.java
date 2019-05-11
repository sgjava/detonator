/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.plugins;

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
     * Database password.
     */
    @Parameter(property = "dbPassword", required = false)
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
     * Map Java types.
     */
    @Parameter(property = "mapTypes", required = true)
    private boolean mapTypes;
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
     * Use database schema to generate code.
     */
    @Parameter(property = "schema", required = false)
    private Schema schema;
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
        final var genCode = new GenCode(getLog());
        genCode.setDbDriver(dbDriver);
        genCode.setDbUrl(dbUrl);
        genCode.setDbUser(dbUser);
        genCode.setDbPassword(dbPassword);
        genCode.setDbPoolSize(dbPoolSize);
        genCode.setMapTypes(mapTypes);
        genCode.setTemplatesDir(templatesDir);
        genCode.setDtoTemplate(dtoTemplate);
        genCode.setIdTemplate(idTemplate);
        genCode.setSqlTemplate(sqlTemplate);
        genCode.setPackageName(packageName);
        genCode.setSqlMap(sqlMap);
        genCode.setSchema(schema);
        return genCode;
    }
}
