<form method="post"  action="?act=SAVE">
<input type="hidden" name="id"  value="<#if entity?has_content>${entity.id}</#if>"/>
<table border="1">
	<tr>
		<td>用户名</td>
		<td><input type="text" name="name"  value="<#if entity?has_content>${entity.name}</#if>"/></td>
	</tr>
	<tr>
		<td>密码</td>
		<td><input type="password" name="password" value="<#if entity?has_content>${entity.password}</#if>"/></td>
	</tr>
	<tr>
		<td colspan="2"><input type="submit"  value="保存"/></td>
	</tr>
</table>
</form>