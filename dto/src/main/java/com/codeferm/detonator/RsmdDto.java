/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package com.codeferm.detonator;

import java.util.Objects;

/**
 * JDBC ResultSetMetaData DTO. Fields have been added to assist with code generation.
 *
 * @author Steven P. Goldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class RsmdDto {

    /**
     * Used during deserialization.
     */
    private static final long serialVersionUID = 1792347180268677569L;

    private String catalogName;
    private String columnClassName;
    private int columnDisplaySize;
    private String columnLabel;
    private String columnName;
    private int columnType;
    private String columnTypeName;
    private Integer keySeq;
    private int precision;
    private int scale;
    private String schemaName;
    private String tableName;
    private boolean autoIncrement;
    private boolean caseSensitive;
    private boolean currency;
    private boolean definitelyWritable;
    private String methodName;
    private int nullable;
    private boolean readOnly;
    private boolean searchable;
    private boolean signed;
    private String varName;
    private String varType;
    private boolean writable;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(final String catalogName) {
        this.catalogName = catalogName;
    }

    public String getColumnClassName() {
        return columnClassName;
    }

    public void setColumnClassName(final String columnClassName) {
        this.columnClassName = columnClassName;
    }

    public int getColumnDisplaySize() {
        return columnDisplaySize;
    }

    public void setColumnDisplaySize(final int columnDisplaySize) {
        this.columnDisplaySize = columnDisplaySize;
    }

    public String getColumnLabel() {
        return columnLabel;
    }

    public void setColumnLabel(final String columnLabel) {
        this.columnLabel = columnLabel;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }

    public int getColumnType() {
        return columnType;
    }

    public void setColumnType(final int columnType) {
        this.columnType = columnType;
    }

    public String getColumnTypeName() {
        return columnTypeName;
    }

    public void setColumnTypeName(final String columnTypeName) {
        this.columnTypeName = columnTypeName;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(final int precision) {
        this.precision = precision;
    }

    public Integer getKeySeq() {
        return keySeq;
    }

    public void setKeySeq(final Integer keySeq) {
        this.keySeq = keySeq;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(final int scale) {
        this.scale = scale;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(final String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(final boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(final boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isCurrency() {
        return currency;
    }

    public void setCurrency(final boolean currency) {
        this.currency = currency;
    }

    public boolean isDefinitelyWritable() {
        return definitelyWritable;
    }

    public void setDefinitelyWritable(final boolean definitelyWritable) {
        this.definitelyWritable = definitelyWritable;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(final String methodName) {
        this.methodName = methodName;
    }

    public int getNullable() {
        return nullable;
    }

    public void setNullable(final int nullable) {
        this.nullable = nullable;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(final boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(final boolean signed) {
        this.signed = signed;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(final String varName) {
        this.varName = varName;
    }

    public String getVarType() {
        return varType;
    }

    public void setVarType(final String varType) {
        this.varType = varType;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(final boolean writable) {
        this.writable = writable;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.catalogName);
        hash = 79 * hash + Objects.hashCode(this.columnClassName);
        hash = 79 * hash + this.columnDisplaySize;
        hash = 79 * hash + Objects.hashCode(this.columnLabel);
        hash = 79 * hash + Objects.hashCode(this.columnName);
        hash = 79 * hash + this.columnType;
        hash = 79 * hash + Objects.hashCode(this.columnTypeName);
        hash = 79 * hash + Objects.hashCode(this.keySeq);
        hash = 79 * hash + this.precision;
        hash = 79 * hash + this.scale;
        hash = 79 * hash + Objects.hashCode(this.schemaName);
        hash = 79 * hash + Objects.hashCode(this.tableName);
        hash = 79 * hash + (this.autoIncrement ? 1 : 0);
        hash = 79 * hash + (this.caseSensitive ? 1 : 0);
        hash = 79 * hash + (this.currency ? 1 : 0);
        hash = 79 * hash + (this.definitelyWritable ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.methodName);
        hash = 79 * hash + this.nullable;
        hash = 79 * hash + (this.readOnly ? 1 : 0);
        hash = 79 * hash + (this.searchable ? 1 : 0);
        hash = 79 * hash + (this.signed ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.varName);
        hash = 79 * hash + Objects.hashCode(this.varType);
        hash = 79 * hash + (this.writable ? 1 : 0);
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
        final RsmdDto other = (RsmdDto) obj;
        if (this.columnDisplaySize != other.columnDisplaySize) {
            return false;
        }
        if (this.columnType != other.columnType) {
            return false;
        }
        if (this.precision != other.precision) {
            return false;
        }
        if (this.scale != other.scale) {
            return false;
        }
        if (this.autoIncrement != other.autoIncrement) {
            return false;
        }
        if (this.caseSensitive != other.caseSensitive) {
            return false;
        }
        if (this.currency != other.currency) {
            return false;
        }
        if (this.definitelyWritable != other.definitelyWritable) {
            return false;
        }
        if (this.nullable != other.nullable) {
            return false;
        }
        if (this.readOnly != other.readOnly) {
            return false;
        }
        if (this.searchable != other.searchable) {
            return false;
        }
        if (this.signed != other.signed) {
            return false;
        }
        if (this.writable != other.writable) {
            return false;
        }
        if (!Objects.equals(this.catalogName, other.catalogName)) {
            return false;
        }
        if (!Objects.equals(this.columnClassName, other.columnClassName)) {
            return false;
        }
        if (!Objects.equals(this.columnLabel, other.columnLabel)) {
            return false;
        }
        if (!Objects.equals(this.columnName, other.columnName)) {
            return false;
        }
        if (!Objects.equals(this.columnTypeName, other.columnTypeName)) {
            return false;
        }
        if (!Objects.equals(this.schemaName, other.schemaName)) {
            return false;
        }
        if (!Objects.equals(this.tableName, other.tableName)) {
            return false;
        }
        if (!Objects.equals(this.methodName, other.methodName)) {
            return false;
        }
        if (!Objects.equals(this.varName, other.varName)) {
            return false;
        }
        if (!Objects.equals(this.varType, other.varType)) {
            return false;
        }
        if (!Objects.equals(this.keySeq, other.keySeq)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RsmdDto{" + "catalogName=" + catalogName + ", columnClassName=" + columnClassName + ", columnDisplaySize="
                + columnDisplaySize + ", columnLabel=" + columnLabel + ", columnName=" + columnName + ", columnType=" + columnType
                + ", columnTypeName=" + columnTypeName + ", keySeq=" + keySeq + ", precision=" + precision + ", scale=" + scale
                + ", schemaName=" + schemaName + ", tableName=" + tableName + ", autoIncrement=" + autoIncrement
                + ", caseSensitive=" + caseSensitive + ", currency=" + currency + ", definitelyWritable=" + definitelyWritable
                + ", methodName=" + methodName + ", nullable=" + nullable + ", readOnly=" + readOnly + ", searchable=" + searchable
                + ", signed=" + signed + ", varName=" + varName + ", varType=" + varType + ", writable=" + writable + '}';
    }
}
