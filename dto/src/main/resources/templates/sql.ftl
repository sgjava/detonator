#
# Created by DeTOnator on ${now}.
#
# ${sql}
#

${table?lower_case}_sel = select <#list list as rsmdDto>${rsmdDto.getColumnName()}<#if rsmdDto?has_next>, </#if></#list> from ${table?upper_case} where <#list pk?values as v>${v} = ?<#if v?has_next> and </#if></#list>
${table?lower_case}_ins = insert into ${table?upper_case} (<#list list as rsmdDto>${rsmdDto.getColumnName()}<#if rsmdDto?has_next>, </#if></#list>) values (<#list list as rsmdDto>?<#if rsmdDto?has_next>, </#if></#list>)
${table?lower_case}_upd = update set <#list list as rsmdDto>${rsmdDto.getColumnName()} = ?<#if rsmdDto?has_next>, </#if></#list> where <#list pk?values as v>${v} = ?<#if v?has_next> and </#if></#list>
${table?lower_case}_del = delete from ${table?upper_case} where <#list pk?values as v>${v} = ?<#if v?has_next> and </#if></#list>
