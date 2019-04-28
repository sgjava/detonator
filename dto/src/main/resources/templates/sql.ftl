#
# Created by DeTOnator on ${now}.
#
# ${sql}
#
<#if tables?size == 1>findAll = select <#list map?values as rsmdDto>${rsmdDto.getColumnName()}<#if rsmdDto?has_next>, </#if></#list> from ${table?upper_case}
findById = select <#list map?values as rsmdDto>${rsmdDto.getColumnName()}<#if rsmdDto?has_next>, </#if></#list> from ${table?upper_case} where <#list pkSet as v>${v} = ?<#if v?has_next> and </#if></#list>
save = insert into ${table?upper_case} (<#list map?values as rsmdDto>${rsmdDto.getColumnName()}<#if rsmdDto?has_next>, </#if></#list>) values (<#list map?values as rsmdDto>?<#if rsmdDto?has_next>, </#if></#list>)
update = update ${table?upper_case} set <#list map?values as rsmdDto>${rsmdDto.getColumnName()} = ?<#if rsmdDto?has_next>, </#if></#list> where <#list pkSet as v>${v} = ?<#if v?has_next> and </#if></#list>
delete = delete from ${table?upper_case} where <#list pkSet as v>${v} = ?<#if v?has_next> and </#if></#list><#else>findAll = ${sql}</#if>
