<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<html>
<head>
<title>wenba scheduler BI</title>
</head>
<body>
	<br />
	<div align="center">
		<table>
			<tr>
				<td align="center">Scheduler ID</td>
				<td align="center">${schedulerId}</td>
				<td colspan="1" align="center">总访问量</td>
				<td colspan="4" align="center">${allRequestNum}次</td>
				<td></td>
			</tr>
			<tr>
				<td colspan="8"><br /></td>
			</tr>
			<tr>
				<th>CNN服务器ID</th>
				<th>服务器Url</th>
				<th>请求数</th>
				<th>第1次成功数</th>
				<th>第1次失败数</th>
				<th>第2次成功数</th>
				<th>第2次失败数</th>
				<th>成功率</th>
			</tr>
			<c:forEach items="${cnnServerList}" var="cnnServer">
				<tr>
					<td align="center">${cnnServer.id}</td>
					<td align="center">${cnnServer.url}</td>
					<td align="center">${cnnServer.ocrServerStatistics.accessNum}次</td>
					<td align="center">${cnnServer.ocrServerStatistics.firstSuccessNum}次</td>
					<td align="center">${cnnServer.ocrServerStatistics.firstFailNum}次</td>
					<td align="center">${cnnServer.ocrServerStatistics.secondSuccessNum}次</td>
					<td align="center">${cnnServer.ocrServerStatistics.secondFailNum}次</td>
					<td align="center"><c:choose>
							<c:when test="${cnnServer.ocrServerStatistics.accessNum=='0'}">0%</c:when>
							<c:otherwise>
								<fmt:formatNumber
									value="${(cnnServer.ocrServerStatistics.firstSuccessNum+cnnServer.ocrServerStatistics.secondSuccessNum)/cnnServer.ocrServerStatistics.accessNum*100}"
									pattern="##.##" minFractionDigits="2"></fmt:formatNumber>
							%</c:otherwise>
						</c:choose></td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="8"><br /></td>
			</tr>
			<tr>
				<th>JAVA服务器ID</th>
				<th>服务器Url</th>
				<th>请求数</th>
				<th>第1次成功数</th>
				<th>第1次失败数</th>
				<th>第2次成功数</th>
				<th>第2次失败数</th>
				<th>成功率</th>
			</tr>
			<c:forEach items="${javaServerList}" var="javaServer">
				<tr>
					<td align="center">${javaServer.id}</td>
					<td align="center">${javaServer.url}</td>
					<td align="center">${javaServer.ocrServerStatistics.accessNum}次</td>
					<td align="center">${javaServer.ocrServerStatistics.firstSuccessNum}次</td>
					<td align="center">${javaServer.ocrServerStatistics.firstFailNum}次</td>
					<td align="center">${javaServer.ocrServerStatistics.secondSuccessNum}次</td>
					<td align="center">${javaServer.ocrServerStatistics.secondFailNum}次</td>
					<td align="center"><c:choose>
							<c:when test="${javaServer.ocrServerStatistics.accessNum=='0'}">0%</c:when>
							<c:otherwise>
								<fmt:formatNumber
									value="${(javaServer.ocrServerStatistics.firstSuccessNum+javaServer.ocrServerStatistics.secondSuccessNum)/javaServer.ocrServerStatistics.accessNum*100}"
									pattern="##.##" minFractionDigits="2"></fmt:formatNumber>
							%</c:otherwise>
						</c:choose></td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="8"><br /></td>
			</tr>
			<tr>
				<th>Search服务器ID</th>
				<th>服务器Url</th>
				<th>请求数</th>
				<th>第1次成功数</th>
				<th>第1次失败数</th>
				<th>第2次成功数</th>
				<th>第2次失败数</th>
				<th>成功率</th>
			</tr>
			<c:forEach items="${searchServerList}" var="searchServer">
				<tr>
					<td align="center">${searchServer.id}</td>
					<td align="center">${searchServer.url}</td>
					<td align="center">${searchServer.searchServerStatistics.accessNum}次</td>
					<td align="center">${searchServer.searchServerStatistics.firstSuccessNum}次</td>
					<td align="center">${searchServer.searchServerStatistics.firstFailNum}次</td>
					<td align="center">${searchServer.searchServerStatistics.secondSuccessNum}次</td>
					<td align="center">${searchServer.searchServerStatistics.secondFailNum}次</td>
					<td align="center"><c:choose>
							<c:when
								test="${searchServer.searchServerStatistics.accessNum=='0'}">0%</c:when>
							<c:otherwise>
								<fmt:formatNumber
									value="${(searchServer.searchServerStatistics.firstSuccessNum+searchServer.searchServerStatistics.secondSuccessNum )/searchServer.searchServerStatistics.accessNum*100}"
									pattern="##.##" minFractionDigits="2"></fmt:formatNumber>
							%</c:otherwise>
						</c:choose></td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="8"><br /></td>
			</tr>
			<tr>
				<td colspan="8" align="center"><input type="button"
					name="Submit" onclick="javascript:history.back(-1);" value="返回"></td>
			</tr>
		</table>
	</div>
</body>
</html>