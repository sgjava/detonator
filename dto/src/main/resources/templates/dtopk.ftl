/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package ${packageName};

<#assign imports = imports + [ "java.io.Serializable" ] />
<#assign imports = imports + [ "java.util.Objects" ] />
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
    private ${rsmdDto.getVarType()} ${rsmdDto.getVarName()};
</#list>
<#if pkMap?has_content>
    /**
     * Primary key.
     */
    private ${className}Id key;
</#if>

    /**
     * Default constructor.
     */
    public ${className}() {
<#if pkMap?has_content>
        key = new ${className}Id();
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
    public ${className}Id getKey() {
        return key;
    }

    /**
     * Mutator for field key.
     *
     * @param key Set key.
     */
    public void setKey(final ${className}Id key) {
        this.key = key;
    }
</#if>

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
        return <#list map?values as rsmdDto><#if !rsmdDto?is_first>                </#if>Objects.equals(${rsmdDto.getVarName()}, obj.${rsmdDto.getVarName()})<#if rsmdDto?has_next> && </#if>
</#list>;
    }

    /**
     * Hash code method.
     *
     * @return Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(
<#list map?values as rsmdDto>                ${rsmdDto.getVarName()}<#if rsmdDto?has_next>, </#if>
</#list>                );
    }

    /**
     * Generated toString method.
     *
     * @return String representation of object.
     */
    @Override
    public String toString() {
        return "${className}{" +
<#list map?values as rsmdDto>                <#if rsmdDto?is_first>"${rsmdDto.getVarName()}="<#else>", ${rsmdDto.getVarName()}="</#if> + ${rsmdDto.getVarName()} +
</#list>                "}";
    }
}
