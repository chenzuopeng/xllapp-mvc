<html>
<head>
<title>错误页面</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
<h4>错误信息: ${throwable.getLocalizedMessage()}</h4>
<pre>
${ExceptionUtils.getStackTrace(throwable)}
</pre>
</body>
</html>