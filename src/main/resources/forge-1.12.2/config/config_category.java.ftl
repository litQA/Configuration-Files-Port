<#if !data.defining>CATEGORY_STACK.push(CATEGORY);
CATEGORY = (CATEGORY.isEmpty() ? "" : CATEGORY + ".") + "${StringUtils.camelToSnake(field$name?replace(" ", ""))?lower_case}";</#if>
    ${statement$write}
<#if !data.defining>CATEGORY = CATEGORY_STACK.pop();</#if>
