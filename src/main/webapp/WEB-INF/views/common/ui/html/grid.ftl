
<#--
      表格
      参数说明：
          
          grid: 表格定义
          data: 表格数据,默认取当前页面中page对象的result属性值.
-->
<#macro grid grid=gridObject data=page.result>
 
<#if (grid.editables?size > 0) && grid.editables?seq_contains("ADD")>   
<table id="grid_top_panel" border="1"  width="600">
	<tr>
		<td align="right"><@add_button/>&nbsp;&nbsp;</td>
	</tr>
</table>
</#if>
    
<table id="${grid.name}"  border="1" width="600">
	<tr align="center">
	     <#list grid.columns as column>
		 <th>${column.label}</th>
         </#list>
         <#if (grid.editablesWithNotADD?size > 0)>
         <th>操作</th>
         </#if>
	</tr>
	<#list data as item>
	<tr align="center">
	    <#list grid.columns as column>
		<td><@bean_property bean=item propertyName=column.name /></td>
		</#list>
		<#if (grid.editablesWithNotADD?size > 0)>
		<td><span id="eidt_panel"><@edit_buttons id=item.id buttons=grid.editablesWithNotADD/></span></td>
	    </#if>
    </tr>
	</#list>
</table>
<table id="grid_bottom_panel" border="1" width="600">
	<tr align="center">
		<td><@page_navigator/></td>
	</tr>
</table>
</#macro>

<#macro edit_buttons id buttons>
    <#list buttons as button>
    	 <#switch button>
	     <#case "UPDATE">
             <@update_button id=id />
	     <#break>
	     <#case "DELETE">
             <@delete_button id=id />
		<#break>
	    </#switch>
	    <#if button_has_next>|</#if>
    </#list>
</#macro>

<#macro add_button label="新增">
<a href="?act=SHOW_FORM">${label}</a>
</#macro>

<#macro update_button id label="修改">
<a href="?act=SHOW_FORM&id=${id}">${label}</a>
</#macro>

<#macro delete_button id label="删除">
<a href="?act=DELETE&ids=${id}">${label}</a>
</#macro>
