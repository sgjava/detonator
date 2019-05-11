/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.plugins;

import java.util.Objects;

/**
 * Schema parameter for mojo.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class Schema {

    /**
     * A catalog name must match the catalog name as it is stored in the database; "" retrieves those without a catalog; null means
     * that the catalog name should not be used to narrow the search.
     */
    private String catalog;
    /**
     * A schema name pattern must match the schema name as it is stored in the database; "" retrieves those without a schema; null
     * means that the schema name should not be used to narrow the search.
     */
    private String schemaPattern;
    /**
     * A table name pattern must match the table name as it is stored in the database.
     */
    private String tableNamePattern;

    /**
     * Default constructor.
     */
    public Schema() {
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(final String catalog) {
        this.catalog = catalog;
    }

    public String getSchemaPattern() {
        return schemaPattern;
    }

    public void setSchemaPattern(final String schemaPattern) {
        this.schemaPattern = schemaPattern;
    }

    public String getTableNamePattern() {
        return tableNamePattern;
    }

    public void setTableNamePattern(final String tableNamePattern) {
        this.tableNamePattern = tableNamePattern;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.catalog);
        hash = 97 * hash + Objects.hashCode(this.schemaPattern);
        hash = 97 * hash + Objects.hashCode(this.tableNamePattern);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Schema other = (Schema) obj;
        if (!Objects.equals(this.catalog, other.catalog)) {
            return false;
        }
        if (!Objects.equals(this.schemaPattern, other.schemaPattern)) {
            return false;
        }
        if (!Objects.equals(this.tableNamePattern, other.tableNamePattern)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Schema{" + "catalog=" + catalog + ", schemaPattern=" + schemaPattern + ", tableNamePattern=" + tableNamePattern
                + '}';
    }
}
