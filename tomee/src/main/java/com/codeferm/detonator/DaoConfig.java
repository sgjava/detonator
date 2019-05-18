/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import javax.sql.DataSource;

/**
 * Simple configuration object for DAOs.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class DaoConfig {

    /**
     * DataSource.
     */
    private DataSource dataSource;
    /**
     * Property file name that contains SQL statements.
     */
    private String propFileName;
    /**
     * Key Class.
     */
    private Class kClass;
    /**
     * Value class.
     */
    private Class vClass;

    /**
     * Default constructor.
     */
    public DaoConfig() {
    }

    /**
     * Construct populated configuration.
     *
     * @param dataSource Database data source.
     * @param propFileName Property file name containing SQL statements.
     * @param kClass Key class type.
     * @param vClass Value class type.
     */
    public DaoConfig(final DataSource dataSource, final String propFileName, final Class kClass, final Class vClass) {
        this.dataSource = dataSource;
        this.propFileName = propFileName;
        this.kClass = kClass;
        this.vClass = vClass;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getPropFileName() {
        return propFileName;
    }

    public void setPropFileName(final String propFileName) {
        this.propFileName = propFileName;
    }

    public Class getkClass() {
        return kClass;
    }

    public void setkClass(final Class kClass) {
        this.kClass = kClass;
    }

    public Class getvClass() {
        return vClass;
    }

    public void setvClass(final Class vClass) {
        this.vClass = vClass;
    }
}
