<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page
	import="org.mylife.home.components.providers.ComponentConfiguration"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%
	List<ComponentConfiguration> data = (List<ComponentConfiguration>)pageContext.getRequest().getAttribute("data");
	Map<String, String> types = (Map<String, String>)pageContext.getRequest().getAttribute("types");
%>
<%!private String getParametersDisplay(ComponentConfiguration item) {
	StringBuffer builder = new StringBuffer();
	for (Map.Entry<String, String> param : item.getParameters().entrySet()) {
		if (builder.length() > 0)
			builder.append("\n");
		builder.append(param.getKey());
		builder.append(" : ");
		builder.append(param.getValue());
	}
	return builder.toString();
}%>

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
				<ul class="tabHeaders">
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
											<th width="150px">Id</th>
											<th width="150px">Type</th>
											<th width="50px">Actif</th>
											<th>Paramètres</th>
											<th width="60px">Actions</th>
										</tr>
									</thead>
									<tbody>

										<%
											for (ComponentConfiguration item : data) {
										%>
										<tr>
											<td><%=WebTools.htmlEscape(item.getComponentId())%></td>
											<td><%=WebTools.htmlEscape(item.getType())%></td>
											<td><a
												href="?action=<%=item.isActive() ? "deactivate" : "activate"%>&id=<%=item.getDataId()%>">
													<img
													src="<%=WebTools.image(pageContext,
						item.isActive() ? "apply.png" : "erase.png")%>"
													title="<%=item.isActive() ? "Oui" : "Non"%>" />
											</a></td>
											<td><%=WebTools.htmlEscape(getParametersDisplay(item))%></td>
											<td><a
												href="?action=updateForm&id=<%=item.getDataId()%>"><img
													src="<%=WebTools.image(pageContext, "modify.png")%>"
													title="Modification" /></a> <a
												href="?action=delete&id=<%=item.getDataId()%>"><img
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

						<form method="post" action="?action=create">

							<table class="form_format">
								<tbody>
									<tr>
										<td>Id :</td>
										<td><input style="width: 100%;" type="text" required
											name="componentId" /></td>
									</tr>
									<tr>
										<td>Type :</td>
										<td><select name="type">
												<%
													boolean first = true;
												%>
												<%
													for (Map.Entry<String, String> type : types.entrySet()) {
												%>
												<option value="<%=type.getKey()%>" <%if (first) {%>
													selected="selected" <%}%>><%=WebTools.htmlEscape(type.getValue())%></option>
												<%
													}
												%>
										</select></td>
									</tr>
									<tr>
										<td>Paramètres :</td>
										<td>
											<div class="table_render_outer">
												<div class="table_render_inner">
													<table class="table_render" id="parameter_table">
														<thead>
															<tr>
																<th>Nom</th>
																<th>Valeur</th>
															</tr>
														</thead>
														<tbody>
														</tbody>
														<tfoot>
															<tr>
																<th colspan="2" style="text-align: left;"><img
																	id="parameter_add"
																	src="<%=WebTools.image(pageContext, "create.png")%>"
																	title="Ajouter ligne" />&nbsp;<img
																	id="parameter_remove"
																	src="<%=WebTools.image(pageContext, "erase.png")%>"
																	title="Supprimer ligne" /></th>
															</tr>
														</tfoot>
													</table>
												</div>
											</div>
										</td>
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

<script>
	$("#parameter_add")
			.click(
					function() {
						var row = '<tr><td><input style="width: 100%;" type="text" required name="nameList" /></td><td><input style="width: 100%;" type="text" required name="valueList" /></td></tr>';
						$('#parameter_table tbody').append(row);
					});
	$("#parameter_remove").click(function() {
		$('#parameter_table tbody tr:last').remove();
	});
</script>
<%@include file="/jsp/template/Footer.jsp"%>