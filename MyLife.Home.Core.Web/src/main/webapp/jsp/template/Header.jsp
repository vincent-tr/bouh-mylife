<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.core.web.WebTools"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css"
	href="<%=WebTools.style(pageContext, "site.css")%>">
<title>MyLife.Home.Core</title>
</head>
<body>

	<div id="container">

		<div id="header">
			<img id="header_img"
				src="<%=WebTools.image(pageContext, "MyLife-96.png")%>" />
			<h1>MyLife.Home.Core</h1>
			<h2><%=pageContext.getRequest().getAttribute("title")%></h2>
		</div>

		<div id="menu">
			<ul>
				<li><a href="<%=WebTools.servlet(pageContext, "console")%>"><img
						src="<%=WebTools.image(pageContext, "home.png")%>" />Console</a></li>
				<li><a
					href="<%=WebTools.servlet(pageContext, "configuration")%>"><img
						src="<%=WebTools.image(pageContext, "wrench.png")%>" />Configuration</a>
				</li>
				<li><a href="<%=WebTools.servlet(pageContext, "plugin")%>"><img
						src="<%=WebTools.image(pageContext, "application.png")%>" />Plugin</a></li>
			</ul>
		</div>

		<div id="content">
			<div id="content_wrapper">