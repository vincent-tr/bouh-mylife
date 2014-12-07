<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css"
	href="<%=WebTools.style(pageContext, "base.css")%>">
<link rel="stylesheet" type="text/css"
	href="<%=WebTools.style(pageContext, "site.css")%>">
<title><%=pageContext.getRequest().getAttribute("app")%></title>

  <script src="http://code.jquery.com/jquery-1.9.1.js"></script>
  <script src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"></script>
  <script src="<%= WebTools.script(pageContext, "jquery.fixheadertable.js") %>"></script>
</head>
<body>

	<div id="container">

		<div id="header">
			<img id="header_img"
				src="<%=WebTools.image(pageContext, "MyLife-96.png")%>" />
			<h1><%=pageContext.getRequest().getAttribute("app")%></h1>
			<h2><%=pageContext.getRequest().getAttribute("title")%></h2>
		</div>

