<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>wenba scheduler config</title>
<link href="../../resources/css/wenba.css" rel="stylesheet"
	type="text/css">
<script type="text/javascript" src="../../resources/js/jquery.js"></script>
<script type="text/javascript" src="../../resources/js/json2.js"></script>
<script type="text/javascript" src="../../resources/js/wenba.js"></script>
<script type="text/javascript">
	
</script>
</head>
<body onload="init();">
	<br />
	<form id="configForm" action="configProcess" method="post">
		<div align="center">

			<table>
				<tr>
					<td align="right">中控服务器ID</td>
					<td align="center">${schedulerId}<input type="hidden" id="schedulerId"
						name="schedulerId" value="${schedulerId}" /></td>
					<td colspan="2"></td>
				</tr>
				<tr>
					<td>CNN服务器集群流量权值</td>
					<td><input type="text" id="ocrServerCnnWeight"
						name="ocrServerCnnWeight" value="${ocrServerCnnWeight}"
						onblur="changeJavaGroupWeight();" />%</td>
					<td>JAVA服务器集群流量权值</td>
					<td><span id="ocrServerJavaWeight"></span>%</td>
				</tr>
				<tr>
					<td align="right">相似度阈值</td>
					<td><input type="text" id="thresUnusedResult"
						name="thresUnusedResult" value="${thresUnusedResult}"
						onblur="checkThresUnusedResult();" /></td>
					<td colspan="2"><font color="red">*该阈值决定是否执行2次OCR</font></td>
				</tr>
				<tr>
					<td align="right">搜索结果个数</td>
					<td><input type="text" id="limit" name="limit"
						value="${limit}" onblur="checkLimitResult();" /></td>
					<td colspan="2"><font color="red">*该值限制搜索返回的结果个数</font></td>
				</tr>
			</table>
			<br />
			<table id="cnnServersTable">
				<tr>
					<th>CNN服务器ID</th>
					<th>服务器状态</th>
					<th>服务器Url</th>
					<th>服务器流量权值</th>
					<th></th>
				</tr>
				<c:forEach items="${cnnServerList}" var="cnnServer">
					<tr>
						<td align="center">${cnnServer.id}<input type="hidden"
							name="cnnServerId" value="${cnnServer.id}" /></td>
						<td align="center">${cnnServer.state}</td>
						<td><input type="text" name="cnnServerUrl"
							value="${cnnServer.url}" onblur="checkNull(this);" /></td>
						<td><input type="text" name="cnnServerWeight"
							value="${cnnServer.weight}" onblur="changeCnnWeight(this)" /></td>
						<td><input type="button" value="删除"
							onclick="deleteCnnServer(this);" /></td>
					</tr>
				</c:forEach>
				<tr>
					<td colspan="4"></td>
					<td align="right"><input type="button" value="添加"
						onclick="addCnnServer();" /></td>
				</tr>
			</table>
			<table id="cnnUnusedServersTable">
				<c:forEach items="${cnnUnusedServerList}" var="cnnUnusedServer">
					<tr>
						<td align="center">${cnnUnusedServer.id}<input type="hidden"
							name="cnnUnusedServerId" value="${cnnUnusedServer.id}" /></td>
						<td align="center">${cnnUnusedServer.state}</td>
						<td align="center">${cnnUnusedServer.url}</td>
						<td align="center">${cnnUnusedServer.weight}</td>
						<td><input type="button" value="删除"
							onclick="deleteCnnUnusedServer(this);" /></td>
					</tr>
				</c:forEach>
			</table>
			<br />
			<table id="javaServersTable">
				<tr>
					<th>JAVA服务器ID</th>
					<th>服务器状态</th>
					<th>服务器Url</th>
					<th>服务器流量权值</th>
					<th></th>
				</tr>
				<c:forEach items="${javaServerList}" var="javaServer">
					<tr>
						<td align="center">${javaServer.id}<input type="hidden"
							name="javaServerId" value="${javaServer.id}" /></td>
						<td align="center">${javaServer.state}</td>
						<td><input type="text" name="javaServerUrl"
							value="${javaServer.url}" onblur="checkNull(this);" /></td>
						<td><input type="text" name="javaServerWeight"
							value="${javaServer.weight}" onblur="changeJavaWeight(this);" /></td>
						<td><input type="button" value="删除"
							onclick="deleteJavaServer(this);" disabled="disabled" /></td>
					</tr>
				</c:forEach>
				<tr>
					<td colspan="4"></td>
					<td align="right"><input type="button" value="添加"
						onclick="addJavaServer();" /></td>
				</tr>
			</table>
			<table id="javaUnusedServersTable">
				<c:forEach items="${javaUnusedServerList}" var="javaUnusedServer">
					<tr>
						<td align="center">${javaUnusedServer.id}<input type="hidden"
							name="javaUnusedServerId" value="${javaUnusedServer.id}" /></td>
						<td align="center">${javaUnusedServer.state}</td>
						<td align="center">${javaUnusedServer.url}</td>
						<td align="center">${javaUnusedServer.weight}</td>
						<td><input type="button" value="删除"
							onclick="deleteJavaUnusedServer(this);" disabled="disabled" /></td>
					</tr>
				</c:forEach>
			</table>
			<br />
			<table>
				<tr>
					<th>Search服务器ID</th>
					<th>服务器状态</th>
					<th>服务器Url</th>
					<th>服务器流量权值</th>
					<th></th>
				</tr>
				<c:forEach items="${searchServerList}" var="searchServer">
					<tr>
						<td align="center">${searchServer.id}<input type="hidden"
							name="searchServerId" value="${searchServer.id}" /></td>
						<td align="center">${searchServer.state}</td>
						<td><input type="text" name="searchServerUrl"
							value="${searchServer.url}" onblur="checkNull(this);" /></td>
						<td><input type="text" name="searchServerWeight" value="100"
							onblur="" disabled="disabled" />%</td>
						<td><input type="button" value="删除" onclick=""
							disabled="disabled" /></td>
					</tr>
				</c:forEach>
				<tr>
					<td colspan="4"></td>
					<td align="right"><input type="button" value="添加" onclick=""
						disabled="disabled" /></td>
				</tr>
			</table>
			<br />
			<table>
				<tr>
					<th>BI服务器ID</th>
					<th>服务器状态</th>
					<th>服务器Url</th>
					<th>服务器流量权值</th>
					<th></th>
				</tr>
				<c:forEach items="${biServerList}" var="biServer">
					<tr>
						<td align="center">${biServer.id}<input type="hidden"
							name="biServerId" value="${biServer.id}" /></td>
						<td align="center">${biServer.state}</td>
						<td><input type="text" name="biServerUrl"
							value="${biServer.url}" onblur="checkNull(this);" /></td>
						<td><input type="text" name="biServerWeight" value="100"
							onblur="" disabled="disabled" />%</td>
						<td><input type="button" value="删除" onclick=""
							disabled="disabled" /></td>
					</tr>
				</c:forEach>
				<tr>
					<td colspan="4"></td>
					<td align="right"><input type="button" value="添加" onclick=""
						disabled="disabled" /></td>
				</tr>
			</table>
			<br />
			<table>
				<tr>
					<td><input type="hidden" value="" name="configData" /></td>
					<td><input type="submit" value="提交" /></td>
					<td><a href="bi"><font color=blue>统计信息</font></a></td>
				</tr>
			</table>
		</div>
	</form>
</body>
</html>