<form method="post"  action="?act=QUERY">
<table border="1" width="600">
	<tr>
		<td>用户名</td>
		<td><input type="text" name="name"/></td>
	</tr>
	<tr align="center">
		<td colspan="2" ><input type="submit"  value="查询"/></td>
	</tr>
</table>
</form>

<table border="1"  width="600">
	<tr>
		<td align="right"><a href="?act=SHOW_FORM">新增</a>&nbsp;&nbsp;</td>
	</tr>
</table>
<table border="1" width="600">
	<tr align="center">
		<td>id</td>
		<td>用户</td>
		<td>密码</td>
		<td>操作</td>
	</tr>
	<#list page.result as item>
	<tr align="center">
		<td>${item.id!}</td>
		<td>${item.name!}</td>
		<td>${item.password!}</td>
		<td><span id=""><a href="?act=SHOW_FORM&id=${item.id!}">修改</a>|<a href="?act=DELETE&ids=${item.id!}">删除</a></span></td>
	</tr>
	</#list>
</table>
<table border="1" width="600">
	<tr>
		<td><@page_navigator/></td>
	</tr>
</table>
