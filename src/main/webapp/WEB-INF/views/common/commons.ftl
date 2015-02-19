<!--
   html页面的头
-->
<#macro html_page_head page=pageObject>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>${page.title!}</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <#include "head.ftl"/>
</head>
<body>
</#macro>

<!--
   html页面的尾
-->
<#macro html_page_bottom>
</body>
</html>
</#macro>