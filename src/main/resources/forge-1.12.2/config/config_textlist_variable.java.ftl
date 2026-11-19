<#if data.defining>
List<? extends String>
<#else>
"${StringUtils.camelToSnake(field$name?replace(" ", ""))?lower_case}", new String[]{
    <#list field_list$entry as entry>
        "${entry}"<#sep>,
    </#list>
}
</#if>
