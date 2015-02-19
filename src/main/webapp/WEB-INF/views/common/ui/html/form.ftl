
<#--
      表单
      参数说明：
          
          form: 表单定义
          data: 表格数据,默认取当前页面中entity对象.
-->
<#macro form form=formObject data=entity! >

<#local action = form.action?lower_case>

<#switch action>
	     <#case "save">
              <#local action_url = '?act=SAVE'>
	     <#break>
	     <#case "query">
              <#local action_url = '?act=QUERY'>
	     <#break>
	     <#case "other">
              <#local action_url = form.url! >
	     <#break>
</#switch>

<form id="${form.name}" method="${form.method}"  action="${action_url}">
<#list form.getHiddenFields() as item>
<@field  field=item data=BeanUtils.getPropertyValueAsString(entity,item.name)/>
</#list>

<table id="${form.name}_table" border="1" width="600">
<#list form.fields as item>
	<tr>
		<td>${item.label}</td>
		<td><@field  field=item data=BeanUtils.getPropertyValueAsString(entity,item.name)/></td>
	</tr>
</#list>
	<tr>
		<td  colspan="2" align="center"><#list form.buttons as item><@button  button=item /><#if item_has_next>&nbsp;</#if></#list></td>
	</tr>
</table>

</form>
</#macro>

<#macro field field data=''>

<#local type = field.getType()?lower_case>

<#switch type>
	     <#case "combo">
	          <select id="${field.name}" name="${field.name}" >
	                <#list field.options as option>
	                <option value ="${option?first}"  <#if option?first==data >selected</#if> >${option?last}</option>
	                </#list>
	          </select>
	     <#break>
	     <#case "date">
             <input type="text" id="${field.name}" name="${field.name}"  value="<#if data?has_content>${data}<#elseif field.initValue?has_content>${field.initValue?datetime}</#if>"   onclick="WdatePicker({el:'${field.name}',dateFmt:'<#if field.dateFormat?has_content>${field.dateFormat}<#else>yyyy-M-d H:mm:ss</#if>'})"  <#if field.readOnly>readonly="readonly"</#if> />
	     <#break>
	     <#default>
	         <input type="${type}" id="${field.name}" name="${field.name}"  value="<#if data?has_content>${data}<#else>${field.initValue!}</#if>"  <#if field.readOnly>readonly="readonly"</#if> />
</#switch>
</#macro>

<#macro button button>
<#local type = button.getType()?lower_case>
<input id="${button.name}" type="${type}"  value="${button.text}"  <#if button.onClickListener?has_content>onClick="${button.onClickListener}"</#if>/><#t>
</#macro>