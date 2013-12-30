<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.ui.web.WebUiTools"%>
<html lang="en" ng-app>
<head>
<meta charset="utf-8">
<title>MyLife.Home.UI.Web</title>
<!-- <link rel="stylesheet" href="css/app.css"> -->
<link rel="stylesheet"
	href="<%=WebUiTools.root(pageContext)%>/lib/bootstrap/css/bootstrap.css">
<script src="<%=WebUiTools.root(pageContext)%>/lib/angular/angular.js"></script>
<script src="<%=WebTools.script(pageContext, "app.js")%>"></script>
<script src="<%=WebTools.script(pageContext, "controllers.js")%>"></script>
<script src="<%=WebTools.script(pageContext, "net.js")%>"></script>
<script src="<%=WebTools.script(pageContext, "structure.js")%>"></script>
<script src="<%=WebTools.script(pageContext, "images.js")%>"></script>
</head>
<body>

	<div ng-view></div>

</body>
</html>
