//init
function init() {
	changeJavaGroupWeight();
	// form submit
	$(document)
			.ready(
					function() {
						$("#configForm")
								.submit(
										function() {
											var configData = document
													.getElementsByName('configData');
											configData[0].value = "";

											var schedulerId = document
													.getElementById('schedulerId');
											var schedulerIdJson = '"schedulerId":"'
													+ schedulerId.value + '"';
											configData[0].value += '{'
													+ schedulerIdJson;

											var ocrServerCnnWeight = document
													.getElementById('ocrServerCnnWeight');
											var ocrServerWeightJson = ',"ocrServerCnnWeight":'
													+ ocrServerCnnWeight.value;
											configData[0].value += ocrServerWeightJson;

											var thresUnusedResult = document
													.getElementById('thresUnusedResult');
											var thresUnusedResultJson = ',"thresUnusedResult":"'
													+ thresUnusedResult.value
													+ '"';
											configData[0].value += thresUnusedResultJson;

											var limit = document
													.getElementById('limit');
											var limitJson = ',"limit":'
													+ limit.value;
											configData[0].value += limitJson;

											var cnnServerIds = document
													.getElementsByName("cnnServerId");
											var cnnServerUrls = document
													.getElementsByName("cnnServerUrl");
											var cnnServerWeights = document
													.getElementsByName("cnnServerWeight");
											if (cnnServerIds.length > 0) {
												var cnnServers = ',"cnnServers" : [ {"id" : "'
														+ cnnServerIds[0].value
														+ '","url" : "'
														+ cnnServerUrls[0].value
														+ '","weight" : '
														+ cnnServerWeights[0].value
														+ '}';
												for (var i = 1; i < cnnServerIds.length; ++i) {
													cnnServers += ',{'
															+ '"id" : "'
															+ cnnServerIds[i].value
															+ '","url" : "'
															+ cnnServerUrls[i].value
															+ '","weight" : '
															+ cnnServerWeights[i].value
															+ '}';
												}
												cnnServers += "]";
												configData[0].value += cnnServers;
											}
											var javaServerIds = document
													.getElementsByName("javaServerId");
											var javaServerUrls = document
													.getElementsByName("javaServerUrl");
											var javaServerWeights = document
													.getElementsByName("javaServerWeight");
											if (javaServerIds.length > 0) {
												var javaServers = ',"javaServers" : [ {"id" : "'
														+ javaServerIds[0].value
														+ '","url" : "'
														+ javaServerUrls[0].value
														+ '","weight" : '
														+ javaServerWeights[0].value
														+ '}';
												for (var i = 1; i < javaServerIds.length; ++i) {
													javaServers += ',{'
															+ '"id" : "'
															+ javaServerIds[i].value
															+ '","url" : "'
															+ javaServerUrls[i].value
															+ '","weight" : '
															+ javaServerWeights[i].value
															+ '}';
												}
												javaServers += "]";
												configData[0].value += javaServers;
											}
											var searchServerIds = document
													.getElementsByName("searchServerId");
											var searchServerUrls = document
													.getElementsByName("searchServerUrl");
											var searchServerWeights = document
													.getElementsByName("searchServerWeight");
											if (searchServerIds.length > 0) {
												var searchServers = ',"searchServers" : [ {"id" : "'
														+ searchServerIds[0].value
														+ '","url" : "'
														+ searchServerUrls[0].value
														+ '","weight" : '
														+ searchServerWeights[0].value
														+ '}';
												for (var i = 1; i < searchServerIds.length; ++i) {
													searchServers += ',{'
															+ '"id" : "'
															+ searchServerIds[i].value
															+ '","url" : "'
															+ searchServerUrls[i].value
															+ '","weight" : '
															+ searchServerWeights[i].value
															+ '}';
												}
												searchServers += "]";
												configData[0].value += searchServers;
											}
											var biServerIds = document
													.getElementsByName("biServerId");
											var biServerUrls = document
													.getElementsByName("biServerUrl");
											var biServerWeights = document
													.getElementsByName("biServerWeight");
											if (biServerIds.length > 0) {
												var biServers = ',"biServers" : [ {"id" : "'
														+ biServerIds[0].value
														+ '","url" : "'
														+ biServerUrls[0].value
														+ '","weight" : '
														+ biServerWeights[0].value
														+ '}';
												for (var i = 1; i < biServerIds.length; ++i) {
													biServers += ',{'
															+ '"id" : "'
															+ biServerIds[i].value
															+ '","url" : "'
															+ biServerUrls[i].value
															+ '","weight" : '
															+ biServerWeights[i].value
															+ '}';
												}
												biServers += "]";
												configData[0].value += biServers;
											}
											configData[0].value += "}";
											// alert(configData[0].value);
											return true;
										});
					});
}

// 修改java服务器集群weight
function changeJavaGroupWeight() {
	var re = /^([1-9]\d*|[0]{1,1})$/;
	var ocrServerCnnWeight = document.getElementById('ocrServerCnnWeight');
	if (!re.test(ocrServerCnnWeight.value)) {
		alert("请输入正整数或者0！");
		ocrServerCnnWeight.focus();
		return;
	}
	if (ocrServerCnnWeight.value < 0 || ocrServerCnnWeight.value > 100) {
		alert("权值必须大于等于0并且小于等于100");
		ocrServerCnnWeight.focus();
		return;
	}
	var weight = 100 - ocrServerCnnWeight.value;
	$("#ocrServerJavaWeight").text(weight);
	return;
}

// 检查相似度阈值设置
function checkThresUnusedResult() {
	var re = /^([0-9]\d{0,1}\.\d{0,1}|[0]{1,1}|[1-9]\d*)$/;
	var thresUnusedResult = document.getElementById('thresUnusedResult');
	if (!re.test(thresUnusedResult.value)) {
		alert("请输入100以内的正数或者0！");
		thresUnusedResult.focus();
		return;
	}
	if (thresUnusedResult.value < 0 || thresUnusedResult.value > 100) {
		alert("阈值必须大于等于0并且小于等于100");
		thresUnusedResult.focus();
		return;
	}
	return;
}

// 检查搜索返回个数限制设置
function checkLimitResult() {
	var re = /^([1-9]\d*|[0]{1,1})$/;
	var limit = document.getElementById('limit');
	if (!re.test(limit.value)) {
		alert("请输入正整数或者0！");
		limit.focus();
		return;
	}
	if (limit.value < 0 || limit.value > 10) {
		alert("搜索个数必须大于等于0并且小于等于10");
		limit.focus();
		return;
	}
	return;
}

// 检查非空
function checkNull(r) {
	if (r.value == "") {
		alert("该值不能为空！");
		r.focus();
		return;
	}
}

// 修改cnn服务器weight
function changeCnnWeight(cnnServer) {
	var re = /^([1-9]\d*|[0]{1,1})$/;
	if (!re.test(cnnServer.value)) {
		alert("请输入正整数或者0！");
		cnnServer.focus();
		return;
	}
	return;
}

// 修改java服务器weight
function changeJavaWeight(javaServer) {
	var re = /^([1-9]\d*|[0]{1,1})$/;
	if (!re.test(javaServer.value)) {
		alert("请输入正整数或者0！");
		javaServer.focus();
		return;
	}
	return;
}

// add new cnn server
function addCnnServer() {
	var cnnServer = document.getElementById('cnnServersTable').insertRow(
			document.getElementById('cnnServersTable').rows.length - 1);

	var id = cnnServer.insertCell(0);
	id.align = "center";
	id.innerHTML = "<input type='text' name='cnnServerId' value='' onblur='checkNull(this);' />";
	var state = cnnServer.insertCell(1);
	state.align = "center";
	state.innerHTML = "IDLE";
	var url = cnnServer.insertCell(2);
	url.innerHTML = "<input type='text' name='cnnServerUrl' value='' onblur='checkNull(this);' />";
	var weight = cnnServer.insertCell(3);
	weight.innerHTML = "<input type='text' name='cnnServerWeight' value='0' onblur='changeCnnWeight(this);' />";
	var del = cnnServer.insertCell(4);
	del.innerHTML = "<input type='button' value='删除' onclick='deleteCnnServer(this);' />";

	var cnnServerIds = document.getElementsByName("cnnServerId");
	cnnServerIds[cnnServerIds.length - 1].focus();
}

// 检查是否有相同ID
function checkCnnServerId() {
	return;
}

// delete cnn server
function deleteCnnServer(cnnServer) {
	var i = cnnServer.parentNode.parentNode.rowIndex;
	document.getElementById('cnnServersTable').deleteRow(i);
	return;
}

// add new java server
function addJavaServer() {
	var javaServer = document.getElementById('javaServersTable').insertRow(
			document.getElementById('javaServersTable').rows.length - 1);
	var id = javaServer.insertCell(0);
	id.align = "center";
	id.innerHTML = "<input type='text' name='javaServerId' value='' onblur='checkNull(this);' />";
	var state = javaServer.insertCell(1);
	state.align = "center";
	state.innerHTML = "IDLE";
	var url = javaServer.insertCell(2);
	url.innerHTML = "<input type='text' name='javaServerUrl' value='' onblur='checkNull(this);' />";
	var weight = javaServer.insertCell(3);
	weight.innerHTML = "<input type='text' name='javaServerWeight' value='0' onblur='changeJavaWeight(this);' />";
	var del = javaServer.insertCell(4);
	del.innerHTML = "<input type='button' value='删除' onclick='deleteJavaServer(this);' />";

	var javaServerIds = document.getElementsByName("javaServerId");
	javaServerIds[javaServerIds.length - 1].focus();
}

// delete java server
function deleteJavaServer(javaServer) {
	var i = javaServer.parentNode.parentNode.rowIndex;
	document.getElementById('javaServersTable').deleteRow(i);
	return;
}

// change checkbox state
function changeCheckState(ocrServers, ocrServer) {
	var ocrServers = document.getElementById(ocrServers);
	if (ocrServers.checked) {
		checkAll(ocrServer);
	} else {
		uncheckAll(ocrServer);
	}
}

// checkbox check all
function checkAll(name) {
	var ocrServers = document.getElemntsByName(name);
	if (ocrServers.length) {
		for (var i = 0; i < ocrServers.length; ++i) {
			ocrServers[i].checked = true;
		}
	}
}

// checkbox uncheck all
function uncheckAll(name) {
	var ocrServers = document.getElemntsByName(name);
	if (ocrServers.length) {
		for (var i = 0; i < ocrServers.length; i++) {
			ocrServers[i].checked = false;
		}
	}
}
