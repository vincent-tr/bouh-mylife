<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.Set" %>
<%@ page import="org.mylife.home.core.web.WebTools" %>    
<%@ page import="org.mylife.home.core.data.DataConfiguration" %>    
<% Set<DataConfiguration> data = (Set<DataConfiguration>)pageContext.getRequest().getAttribute("data"); %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>MyLife.Home.Core : Gestion des configurations</title>
</head>
<body>
	<h1>MyLife.Home.Core : Gestion des configurations</h1>
	<table>
		<tr>
			<th>Type</th>
			<th>Actif</th>
			<th>Date</th>
			<th>Commentaires</th>
			<th>Actions</th>
		</tr>
<% for(DataConfiguration item : data) { %>
		<tr>
			<td><%= WebTools.htmlEscape(item.getType()) %></td>
			<td></td>
			<td><%= WebTools.htmlEscape(WebTools.formatDate(item.getDate())) %></td>
			<td><%= WebTools.htmlEscape(item.getComment()) %></td>
			<td></td>
		</tr>
<% } %>		
	</table>
</body>
</html>