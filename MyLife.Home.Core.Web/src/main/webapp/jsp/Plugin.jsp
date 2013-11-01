<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List" %>
<%@ page import="org.mylife.home.core.web.WebTools" %>    
<%@ page import="org.mylife.home.core.data.DataPlugin" %>    
<% List<DataPlugin> data = (List<DataPlugin>)pageContext.getRequest().getAttribute("data"); %>

<%@include file="/jsp/template/Header.jsp" %>

	<table border="1">
		<tr>
			<th>Nom</th>
			<th>Actif</th>
			<th>Date</th>
			<th>Commentaires</th>
			<th>Actions</th>
		</tr>
<% for(DataPlugin item : data) { %>
		<tr>
			<td><%= WebTools.htmlEscape(item.getName()) %></td>
			<td>
				<a href="?action=<%= item.isActive() ? "deactivate" : "activate" %>&id=<%= item.getId() %>">
					<img src="<%= WebTools.image(pageContext, item.isActive() ? "apply.png" : "erase.png") %>" title="<%= item.isActive() ? "Oui" : "Non" %>" />
				</a>
			</td>
			<td><%= WebTools.htmlEscape(WebTools.formatDate(item.getDate())) %></td>
			<td>
				<form method="post" action="?action=comment&id=<%= item.getId() %>">
					<textarea name="comment" rows="4" cols="120"><%= WebTools.htmlEscape(item.getComment()) %></textarea>
					<input type="image" src="<%= WebTools.image(pageContext, "modify.png") %>" title="Mise à jour" />
				</form>
			</td>
			<td>
				<a href="?action=delete&id=<%= item.getId() %>"><img src="<%= WebTools.image(pageContext, "erase.png") %>" title="Supprimer" /></a>
				<a href="?action=content&id=<%= item.getId() %>"><img src="<%= WebTools.image(pageContext, "view.png") %>" title="Contenu" /></a>
			</td>
		</tr>
<% } %>		
	</table>

<%@include file="/jsp/template/Footer.jsp" %>