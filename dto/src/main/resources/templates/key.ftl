/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package ${packageName};

<#assign imports = imports + [ "java.util.Comparator" ] />
<#assign imports = imports + [ "java.util.Objects" ] />
<#assign imports = imports + [ "java.io.Serializable" ] />
<#list imports as import>
import ${import};
</#list>

/**
 * Created by DeTOnator on ${now}.
 *
 * ${sql}
 */
public class ${className} implements Comparable<${className}>, Serializable {
<#list map?values as rsmdDto>

    /**
     * Mapped from database field ${rsmdDto.getColumnName()}, type ${rsmdDto.getColumnTypeName()}, precision ${rsmdDto.getPrecision()}, scale ${rsmdDto.getScale()}<#if rsmdDto.getKeySeq()??>, PK sequence ${rsmdDto.getKeySeq()}</#if>.
     */
    private ${rsmdDto.getVarType()} ${rsmdDto.getVarName()};
</#list>

    /**
     * Default constructor.
     */
    public ${className}() {
    }

    /**
     * Initialize member vars constructor.
     *
<#list map?values as rsmdDto>
     * @param ${rsmdDto.getVarName()} Set ${rsmdDto.getVarName()}.
</#list>
     */
    public ${className}(<#list map?values as rsmdDto>final ${rsmdDto.getVarType()} ${rsmdDto.getVarName()}<#if rsmdDto?has_next>, </#if></#list>) {
<#list map?values as rsmdDto>
        this.${rsmdDto.getVarName()} = ${rsmdDto.getVarName()};
</#list>
    }

<#list map?values as rsmdDto>
    /**
     * Accessor for field ${rsmdDto.getVarName()}.
     *
     * @return ${rsmdDto.getVarName()} Get ${rsmdDto.getVarName()}.
     */
    public ${rsmdDto.getVarType()} get${rsmdDto.getMethodName()}() {
        return ${rsmdDto.getVarName()};
    }

    /**
     * Mutator for field ${rsmdDto.getVarName()}.
     *
     * @param ${rsmdDto.getVarName()} Set ${rsmdDto.getVarName()}.
     */
    public void set${rsmdDto.getMethodName()}(final ${rsmdDto.getVarType()} ${rsmdDto.getVarName()}) {
        this.${rsmdDto.getVarName()} = ${rsmdDto.getVarName()};
    }

</#list>
    /**
     * Equals method.
     *
     * @param o Object to check for equality.
     * @return True if objects equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ${className})) {
            return false;
        }
        ${className} obj = (${className}) o;
        return <#list map?values as rsmdDto><#if !rsmdDto?is_first>                </#if>Objects.equals(${rsmdDto.getVarName()}, obj.${rsmdDto.getVarName()})<#if rsmdDto?has_next> && </#if><#if rsmdDto?is_last>;</#if>
</#list>
    }

    /**
     * Hash code method.
     *
     * @return Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(
<#list map?values as rsmdDto>                ${rsmdDto.getVarName()}<#if rsmdDto?has_next>, </#if><#if rsmdDto?is_last>);</#if>
</#list>
    }

    /**
     * Generated toString method.
     *
     * @return String representation of object.
     */
    @Override
    public String toString() {
        return "${className}{" +
<#list map?values as rsmdDto>                <#if rsmdDto?is_first>"${rsmdDto.getVarName()}="<#else>", ${rsmdDto.getVarName()}="</#if> + ${rsmdDto.getVarName()} + <#if rsmdDto?is_last>"}";</#if>
</#list>
    }

    /**
     * Generated compareTo method. Compares this object with the specified object for order. Returns a negative integer, zero, or a
     * positive integer as this object is less than, equal to, or greater than the specified object.
     *
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified
     * object.
     */
    @Override
    public int compareTo(${className} o) {
        <#list mapOrder?values as rsmdDto><#if rsmdDto?is_first>return Comparator.comparing(${className}::get${rsmdDto.getMethodName()})<#else>
                .thenComparing(${className}::get${rsmdDto.getMethodName()})</#if></#list>
                .compare(this, o);
    }
}
