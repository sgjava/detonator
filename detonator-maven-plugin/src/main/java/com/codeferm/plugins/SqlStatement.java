/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.plugins;

import java.util.List;
import java.util.Objects;

/**
 * SQL statement parameter for mojo.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class SqlStatement {

    /**
     * SQL statement used for metadata.
     */
    private String sql;
    /**
     * List of primary key overrides.
     */
    private List<String> keyColumns;

    /**
     * Default constructor.
     */
    public SqlStatement() {
    }

    /**
     * All fields constructor.
     *
     * @param sql SQL statement.
     * @param keyColumns Primary key column overrides.
     */
    public SqlStatement(final String sql, final List<String> keyColumns) {
        this.sql = sql;
        this.keyColumns = keyColumns;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<String> getKeyColumns() {
        return keyColumns;
    }

    public void setKeyColumns(List<String> keyColumns) {
        this.keyColumns = keyColumns;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.sql);
        hash = 97 * hash + Objects.hashCode(this.keyColumns);
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
        final SqlStatement other = (SqlStatement) obj;
        if (!Objects.equals(this.sql, other.sql)) {
            return false;
        }
        if (!Objects.equals(this.keyColumns, other.keyColumns)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SqlStatement{" + "sql=" + sql + ", keyColumns=" + keyColumns + '}';
    }
}
