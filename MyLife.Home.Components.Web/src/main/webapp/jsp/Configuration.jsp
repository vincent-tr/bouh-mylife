<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%
	//List<DataConfiguration> data = (List<DataConfiguration>)pageContext.getRequest().getAttribute("data");
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
	<table class="tabHeaders">
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
											<th width="60px">Type</th>
											<th width="50px">Actif</th>
											<th width="150px">Date</th>
											<th>Commentaires</th>
											<th width="60px">Actions</th>
										</tr>
									</thead>
									<tbody>
									
									<!--  TODO  -->
									
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
												<option value="core" selected="selected">Core</option>
												<option value="net">Net</option>
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
											name="content" accept="application/xml" /></td>
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