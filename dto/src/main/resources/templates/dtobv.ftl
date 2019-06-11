/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package ${packageName};

<#assign imports = imports + [ "java.util.Objects" ] />
<#assign imports = imports + [ "java.io.Serializable" ] />
<#list map?values as rsmdDto>
<#if rsmdDto.getNullable() == 0 && !imports?seq_contains("javax.validation.constraints.NotNull")>
<#assign imports = imports + [ "javax.validation.constraints.NotNull" ] />
</#if>
</#list>
<#list imports as import>
import ${import};
</#list>

/**
 * Created by DeTOnator on ${now}.
 *
 * ${sql}
 */
public class ${className} implements Serializable {
<#list map?values as rsmdDto>

    /**
     * Mapped from database field ${rsmdDto.getColumnName()}, type ${rsmdDto.getColumnTypeName()}, precision ${rsmdDto.getPrecision()}, scale ${rsmdDto.getScale()}<#if rsmdDto.getKeySeq()??>, PK sequence ${rsmdDto.getKeySeq()}</#if>.
     */
<#if rsmdDto.getNullable() == 0>
    @NotNull
</#if>
    private ${rsmdDto.getVarType()} ${rsmdDto.getVarName()};
</#list>
<#if pkMap?has_content>
    /**
     * Primary key.
     */
    private ${className}Key key;
</#if>

    /**
     * Default constructor.
     */
    public ${className}() {
<#if pkMap?has_content>
        key = new ${className}Key();
</#if>
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
<#if rsmdDto.getKeySeq()??>
        key.set${rsmdDto.getMethodName()}(${rsmdDto.getVarName()});
</#if>
    }

</#list>
<#if pkMap?has_content>
    /**
     * Accessor for field key.
     *
     * @return key Get key.
     */
    public ${className}Key getKey() {
        return key;
    }

    /**
     * Mutator for field key.
     *
     * @param key Set key.
     */
    public void setKey(final ${className}Key key) {
        this.key = key;
    }
</#if>

    /**
     * Equals method.
     *
     * @param o Object to check for equality.
     * @return True if all objects equal.
     */
    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ${className})) {
            return false;
        }
        ${className} obj = (${className}) o;
        return <#list map?values as rsmdDto><#if !rsmdDto?is_first>                </#if>Objects.equals(${rsmdDto.getVarName()}, obj.${rsmdDto.getVarName()})<#if rsmdDto?has_next || pkMap?has_content> &&</#if><#if rsmdDto?is_last && !pkMap?has_content>;</#if>
</#list><#if pkMap?has_content>                Objects.equals(key, obj.key);</#if>
    }

    /**
     * Hash code method.
     *
     * @return Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(
<#list map?values as rsmdDto>                ${rsmdDto.getVarName()}<#if rsmdDto?has_next || pkMap?has_content>, </#if><#if rsmdDto?is_last && !pkMap?has_content>);</#if>
</#list><#if pkMap?has_content>                key);</#if>
    }

    /**
     * toString method.
     *
     * @return String representation of object.
     */
    @Override
    public String toString() {
        return "${className}{" +
<#list map?values as rsmdDto>                <#if rsmdDto?is_first>"${rsmdDto.getVarName()}="<#else>", ${rsmdDto.getVarName()}="</#if> + ${rsmdDto.getVarName()} + <#if rsmdDto?is_last && !pkMap?has_content>"}";</#if>
</#list><#if pkMap?has_content>                ", key=" + key + "}";</#if>
    }
}
