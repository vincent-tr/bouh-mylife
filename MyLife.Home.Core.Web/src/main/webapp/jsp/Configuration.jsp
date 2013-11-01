<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List" %>
<%@ page import="org.mylife.home.core.web.WebTools" %>    
<%@ page import="org.mylife.home.core.data.DataConfiguration" %>    
<% List<DataConfiguration> data = (List<DataConfiguration>)pageContext.getRequest().getAttribute("data"); %>

<%@include file="/jsp/template/Header.jsp" %>

	<h2>Gestion des configurations</h2>
	<table border="1">
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
			<td>
				<a href="?action=<%= item.isActive() ? "deactivate" : "activate" %>&id=<%= item.getId() %>">
					<img src="<%= WebTools.image(pageContext, item.isActive() ? "apply.png" : "erase.png") %>" title="<%= item.isActive() ? "Oui" : "Non" %>" />
				</a>
			</td>
			<td><%= WebTools.htmlEscape(WebTools.formatDate(item.getDate())) %></td>
			<td>
				<form method="post" action="?action=comment&id=<%= item.getId() %>">
					<textarea name="comment"><%= WebTools.htmlEscape(item.getComment()) %></textarea>
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
	
	<div>
		<div>
			<form method="post" action="?action=create" enctype="multipart/form-data">
				<select name="type">
					<option value="core" selected="selected">Core</option>
					<option value="net">Net</option>
				</select>
				<textarea name="comment"></textarea>
				<input type="file" name="content" accept="application/xml" />
				<input type="image" src="<%= WebTools.image(pageContext, "create.png") %>" title="Création" />
			</form>
		</div>
		<div>
			<form method="post" action="?action=contentCreate" enctype="multipart/form-data">
				<input type="file" name="content" accept="application/xml" />
				<input type="image" src="<%= WebTools.image(pageContext, "upload.png") %>" title="Télécharger" />
			</form>
		</div>
		<div>
			<form method="post" action="?action=downloadCreate">
				<input type="text" name="url" />
				<input type="image" src="<%= WebTools.image(pageContext, "download.png") %>" title="Télécharger" />
			</form>
		<div>
	</div>

<%@include file="/jsp/template/Footer.jsp" %>