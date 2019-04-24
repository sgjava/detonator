/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 */
package ${packageName};

<#list imports as import>
import ${import};
</#list>
import java.util.Objects;

/**
 * Created by DeTOnator on ${now}.
 *
 * ${sql}
 */
public class ${className} {
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
        return <#list map?values as rsmdDto>Objects.equals(${rsmdDto.getVarName()}, obj.${rsmdDto.getVarName()})<#if rsmdDto?has_next> && </#if></#list>;
    }

    /**
     * Hash code method.
     *
     * @return Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(<#list map?values as rsmdDto>${rsmdDto.getVarName()}<#if rsmdDto?has_next>, </#if></#list>);
    }

    /**
     * Generated toString method.
     *
     * @return String representation of object.
     */
    @Override
    public String toString() {
        return "${className}{" + <#list map?values as rsmdDto><#if rsmdDto?is_first>"${rsmdDto.getVarName()}="<#else>", ${rsmdDto.getVarName()}="</#if> + ${rsmdDto.getVarName()} + </#list>"}";
    }
}