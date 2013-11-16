<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>

<% pageContext.getRequest().setAttribute("app", "MyLife.Home.Components"); %>

<%@include file="/jsp/template/BaseHeader.jsp"%>

<div id="menu">
	<ul>
		<li><a href="<%=WebTools.servlet(pageContext, "console")%>"><img
				src="<%=WebTools.image(pageContext, "home.png")%>" />Console</a></li>
		<li><a
			href="<%=WebTools.servlet(pageContext, "configuration")%>"><img
				src="<%=WebTools.image(pageContext, "wrench.png")%>" />Configuration</a>
		</li>
	</ul>
</div>

<%@include file="/jsp/template/BaseHeaderAfterMenu.jsp"%>
