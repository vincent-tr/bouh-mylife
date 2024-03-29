<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.ui.web.WebUiTools"%>
<html lang="en" ng-app="mylife.app">
<head>
<meta charset="utf-8">
<title>MyLife.Home.UI.Web</title>
<link rel="stylesheet" href="<%=WebUiTools.root(pageContext)%>/lib/bootstrap/css/bootstrap.css">
<link rel="stylesheet" href="<%=WebTools.style(pageContext, "app.css")%>">
<script src="<%=WebUiTools.root(pageContext)%>/lib/angular/angular.js"></script>
<script src="<%=WebUiTools.root(pageContext)%>/lib/angular/angular-route.js"></script>
<script src="<%=WebUiTools.root(pageContext)%>/lib/angular-ui/ui-bootstrap-tpls.js"></script>
<script src="<%=WebUiTools.root(pageContext)%>/lib/angular-websocket/angular-websocket.js"></script>
<script src="<%=WebTools.script(pageContext, "app.js")%>"></script>
<script src="<%=WebTools.script(pageContext, "controllers.js")%>"></script>
<script src="<%=WebTools.script(pageContext, "net.js")%>"></script>
<script src="<%=WebTools.script(pageContext, "structure.js")%>"></script>
<script src="<%=WebTools.script(pageContext, "images.js")%>"></script>
<script src="<%=WebTools.script(pageContext, "modelBuilder.js")%>"></script>
<script>
'use strict';
angular.module('mylife.urlHelper', [], function($provide) {
	$provide.provider('urlHelper', function() {
		
		var servlet = function(name) {
			return '<%= WebTools.servlet(pageContext, "")%>' + name;
		};
		
		var partial = function(name) {
			return '<%= WebUiTools.partial(pageContext)%>' + name;
		};
		
		var webSocket = function() {
			return '<%= WebUiTools.webSocketUrl(pageContext)%>';
		};
		
		this.servlet = servlet;
		this.partial = partial;
		this.webSocket = webSocket;
		
		this.$get = function() {
			return {
				servlet : servlet,
				partial : partial,
				webSocket : webSocket,
			};
		};
	});
});
</script>
</head>
<body>

	<div ng-view></div>

</body>
</html>
