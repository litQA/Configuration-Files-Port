<#if data.defining>
public static Property ${field$name?upper_case};
<#else>
${field$name?upper_case} = CONFIG.get(category(), ${input$value}, "<#if field$comment??>${field$comment}</#if>");
</#if>
