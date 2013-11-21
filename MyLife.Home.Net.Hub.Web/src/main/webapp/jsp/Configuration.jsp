<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.net.hub.data.DataLink"%>
<%
	List<DataLink> data = (List<DataLink>)pageContext.getRequest().getAttribute("data");
	Map<String, String> types = (Map<String, String>)pageContext.getRequest().getAttribute("types");
%>

<%@include file="/jsp/template/Header.jsp"%>

<script>
	$(function() {
		$(".tabs").tabs();
	});

	$(document).ready(function() {
		$('.tablerender').fixheadertable({
			caption : '',
			height : 200
		});
	});
</script>

<div class="tabs">
	<table>
		<tr>
			<td>
				<ul>
					<li><a href="#tabs-1"><img
							src="<%=WebTools.image(pageContext, "view.png")%>" />Données</a></li>
					<li><a href="#tabs-2"><img
							src="<%=WebTools.image(pageContext, "create.png")%>" />Création</a></li>
				</ul>
			</td>
		</tr>
		<tr>
			<td>
				<div id="tabs-1">
					<fieldset>
						<legend>
							<span>Données</span>
						</legend>

						<div class="table_render_outer">
							<div class="table_render_inner">
								<table class="table_render">
									<thead>
										<tr>
											<th>Nom</th>
											<th width="120px">Type</th>
											<th>Adresse</th>
											<th width="120px">Port</th>
											<th>Mot de passe</th>
											<th width="60px">Actions</th>
										</tr>
									</thead>
									<tbody>
										<%
											for (DataLink item : data) {
										%>
										<tr>
											<td><%=WebTools.htmlEscape(item.getName())%></td>
											<td><%=WebTools.htmlEscape(item.getType())%></td>
											<td><%=WebTools.htmlEscape(item.getAddress())%></td>
											<td><%=WebTools.htmlEscape(String.valueOf(item.getPort()))%></td>
											<td><%=WebTools.htmlEscape(item.getPassword())%></td>
											<td><a href="?action=delete&id=<%=item.getId()%>"><img
													src="<%=WebTools.image(pageContext, "erase.png")%>"
													title="Supprimer" /></a></td>
										</tr>
										<%
											}
										%>
									</tbody>
								</table>
							</div>
						</div>
					</fieldset>
				</div>

				<div id="tabs-2">
					<fieldset>
						<legend>
							<span>Création</span>
						</legend>

						<form method="post" action="?action=create"
							enctype="multipart/form-data">

							<table class="form_format">
								<tbody>
									<tr>
										<td>Nom :</td>
										<td><input style="width: 100%;" type="text" required name="name" /></td>
									</tr>
									<tr>
										<td>Type :</td>
										<td><select name="type">
												<%
													boolean first = true;
													for (Map.Entry<String, String> type : types.entrySet()) {
												%>
												<option value="<%=WebTools.htmlEscape(type.getKey())%>"
													<%if (first) {%> selected="selected" <%}%>><%=WebTools.htmlEscape(type.getValue())%></option>
												<%
													first = false;
													}
												%>
										</select></td>
									</tr>
									<tr>
										<td>Adresse :</td>
										<td><input style="width: 100%;" type="text" required name="address" /></td>
									</tr>
									<tr>
										<td>Port :</td>
										<td><input style="width: 100%;" type="number" min="1" max="65535" required name="port" /></td>
									</tr>
									<tr>
										<td>Mot de passe :</td>
										<td><input style="width: 100%;" type="text" required name="password" /></td>
									</tr>
									<tr>
										<td colspan="2" class="form_action"><input type="image"
											src="<%=WebTools.image(pageContext, "create.png")%>"
											title="Création" /></td>
									</tr>
								</tbody>
							</table>
						</form>
					</fieldset>
				</div>
			</td>
		</tr>
	</table>
</div>

<%@include file="/jsp/template/Footer.jsp"%>