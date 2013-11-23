<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>

<%
	pageContext.getRequest().setAttribute("app", "MyLife.Home.Core");
%>

<%@include file="/jsp/template/BaseHeader.jsp"%>

<div id="menu">
	<ul>
		<li><a href="<%=WebTools.servlet(pageContext, "console")%>"><img
				src="<%=WebTools.image(pageContext, "home.png")%>" />Console</a></li>
		<li><a href="<%=WebTools.servlet(pageContext, "configuration")%>"><img
				src="<%=WebTools.image(pageContext, "wrench.png")%>" />Configuration</a>
		</li>
		<li><a href="<%=WebTools.servlet(pageContext, "plugin")%>"><img
				src="<%=WebTools.image(pageContext, "application.png")%>" />Plugin</a></li>
		<li><a href="<%=WebTools.servlet(pageContext, "logging")%>"><img
				src="<%=WebTools.image(pageContext, "view.png")%>" />Logging</a></li>
	</ul>
</div>

<%@include file="/jsp/template/BaseHeaderAfterMenu.jsp"%>
