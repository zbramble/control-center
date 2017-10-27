<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<html>
<head>
<title>wenba scheduler login</title>
</head>
<body>
	<br />
	<br />
	<br />
	<br />
	<br />
	<form:form action="loginProcess" method="post" commandName="user">
		<div align="center">
			<table>
				<tr>
					<td>用户名：</td>
					<td><form:input path="userName" /></td>
				</tr>
				<tr>
					<td>密 码：</td>
					<td><form:password path="password" /></td>
				</tr>
				<tr>
					<td colspan="2" align="right"><input type="submit" value="登录"><input
						type="reset" value="重置" /></td>
				</tr>
			</table>
		</div>
	</form:form>
</body>
</html>