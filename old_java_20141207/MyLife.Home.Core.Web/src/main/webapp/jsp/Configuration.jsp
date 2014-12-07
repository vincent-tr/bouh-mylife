<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.core.data.DataConfiguration"%>
<%
	List<DataConfiguration> data = (List<DataConfiguration>)pageContext.getRequest().getAttribute("data");
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
											<th width="60px">Type</th>
											<th width="50px">Actif</th>
											<th width="150px">Date</th>
											<th>Commentaires</th>
											<th width="60px">Actions</th>
										</tr>
									</thead>
									<tbody>
										<%
											for (DataConfiguration item : data) {
										%>
										<tr>
											<td><%=WebTools.htmlEscape(item.getType())%></td>
											<td><a
												href="?action=<%=item.isActive() ? "deactivate" : "activate"%>&id=<%=item.getId()%>">
													<img
													src="<%=WebTools.image(pageContext,
						item.isActive() ? "apply.png" : "erase.png")%>"
													title="<%=item.isActive() ? "Oui" : "Non"%>" />
											</a></td>
											<td><%=WebTools.htmlEscape(WebTools.formatDate(item
						.getDate()))%></td>
											<td>
												<form method="post"
													action="?action=comment&id=<%=item.getId()%>">
													<div
														style="position: relative; height: 70px; vertical-align: middle;">
														<div
															style="position: absolute; top: 2px; bottom: 8px; left: 2px; right: 40px;">
															<textarea
																style="resize: none; margin: 0; width: 100%; height: 100%;"
																name="comment"><%=WebTools.htmlEscape(item.getComment(), false)%></textarea>
														</div>
														<input
															style="position: absolute; top: 50%; margin-top: -12px; right: 5px;"
															type="image"
															src="<%=WebTools.image(pageContext, "modify.png")%>"
															title="Mise à jour" />
													</div>
												</form>
											</td>
											<td><a href="?action=delete&id=<%=item.getId()%>"><img
													src="<%=WebTools.image(pageContext, "erase.png")%>"
													title="Supprimer" /></a> <a
												href="?action=content&id=<%=item.getId()%>"><img
													src="<%=WebTools.image(pageContext, "view.png")%>"
													title="Contenu" /></a></td>
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
							<span>Création manuelle</span>
						</legend>

						<form method="post" action="?action=create"
							enctype="multipart/form-data">

							<table class="form_format">
								<tbody>
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
										<td>Commentaires :</td>
										<td>
											<div
												style="position: relative; height: 70px; vertical-align: middle;">
												<div
													style="position: absolute; top: 2px; bottom: 8px; left: 0px; right: 8px;">
													<textarea
														style="resize: none; margin: 0; width: 100%; height: 100%;"
														name="comment" rows="4" cols="120"></textarea>
												</div>
											</div>
										</td>
									</tr>
									<tr>
										<td>Contenu :</td>
										<td><input style="width: 100%;" type="file"
											name="content" required accept="application/xml" /></td>
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

					<fieldset>
						<legend>
							<span>Création automatique</span>
						</legend>

						<form method="post" action="?action=contentCreate"
							enctype="multipart/form-data">

							<table class="form_format">
								<tbody>
									<tr>
										<td>Contenu :</td>
										<td><input style="width: 100%;" type="file"
											name="content" required accept="application/xml" /></td>
									</tr>
									<tr>
										<td colspan="2" class="form_action"><input type="image"
											src="<%=WebTools.image(pageContext, "upload.png")%>"
											title="Télécharger" /></td>
									</tr>
								</tbody>
							</table>
						</form>
					</fieldset>

					<fieldset>
						<legend>
							<span>Création par téléchargement</span>
						</legend>

						<form method="post" action="?action=downloadCreate">

							<table class="form_format">
								<tbody>
									<tr>
										<td>URL :</td>
										<td><input style="width: 100%;" type="text" required name="url" /></td>
									</tr>
									<tr>
										<td colspan="2" class="form_action"><input type="image"
											src="<%=WebTools.image(pageContext, "download.png")%>"
											title="Télécharger" /></td>
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