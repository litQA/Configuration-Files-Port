<#if data.defining>
List<? extends String>
<#else>
List("${StringUtils.camelToSnake(field$name?replace(" ", ""))?lower_case}", Arrays.asList(
    <#list field_list$entry as entry>
        "${entry}"<#sep>,
    </#list>
    ), entry -> true)
</#if>
