<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List" %>
<%@ page import="org.mylife.home.core.web.WebTools" %>    
<%@ page import="org.mylife.home.core.data.DataConfiguration" %>    
<% List<DataConfiguration> data = (List<DataConfiguration>)pageContext.getRequest().getAttribute("data"); %>

<%@include file="/jsp/template/Header.jsp" %>

	<table class="table_render">
		<thead>
			<tr>
				<th width="60px">Type</th>
				<th width="50px">Actif</th>
				<th width="150px">Date</th>
				<th>Commentaires</th>
				<th width="60px">Actions</th>
			</tr>
		</thead>
		<tbody>
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
						<div style="position: relative; height: 70px; vertical-align: middle;">
							<div style="position: absolute; top: 2px; bottom: 8px; left: 2px; right: 40px;">
								<textarea style="resize: none; margin: 0; width: 100%; height: 100%;" name="comment"><%= WebTools.htmlEscape(item.getComment()) %></textarea>
							</div>
							<input style="position:absolute; top:50%; margin-top:-12px; right: 5px;" type="image" src="<%= WebTools.image(pageContext, "modify.png") %>" title="Mise à jour" />
						</div>
					</form>
				</td>
				<td>
					<a href="?action=delete&id=<%= item.getId() %>"><img src="<%= WebTools.image(pageContext, "erase.png") %>" title="Supprimer" /></a>
					<a href="?action=content&id=<%= item.getId() %>"><img src="<%= WebTools.image(pageContext, "view.png") %>" title="Contenu" /></a>
				</td>
			</tr>
<% } %>		
		</tbody>
	</table>
	
	<div>
		<div>
			<form method="post" action="?action=create" enctype="multipart/form-data">
				<select name="type">
					<option value="core" selected="selected">Core</option>
					<option value="net">Net</option>
				</select>
				<textarea name="comment" rows="4" cols="120"></textarea>
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