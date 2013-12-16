<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page
	import="org.mylife.home.components.providers.ComponentConfiguration"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%
	ComponentConfiguration data = (ComponentConfiguration) pageContext
	.getRequest().getAttribute("data");
%>

<%@include file="/jsp/template/Header.jsp"%>

<div class="tabs">
	<table>
		<tr>
			<td>
				<ul class="tabHeaders">
					<li><a href="#tabs-1"><img
							src="<%=WebTools.image(pageContext, "modify.png")%>" />Modification</a></li>
				</ul>
			</td>
		</tr>
		<tr>
			<td>
				<div id="tabs-1">
					<fieldset>
						<legend>
							<span>Modification</span>
						</legend>

						<form method="post" action="?action=updateParameters&id=<%=data.getDataId()%>">

							<table class="form_format">
								<tbody>
									<tr>
										<td>Id :</td>
										<td><%=WebTools.htmlEscape(data.getComponentId())%></td>
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
															<%
																for(Map.Entry<String, String> param : data.getParameters().entrySet()) {
															%>
															<tr>
																<td><input style="width: 100%;" type="text"
																	required name="nameList" value="<%=WebTools.htmlEscape(param.getKey())%>"/></td>
																<td><input style="width: 100%;" type="text"
																	required name="valueList" value="<%=WebTools.htmlEscape(param.getValue())%>" /></td>
															</tr>
															<%
																}
															%>
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
											src="<%=WebTools.image(pageContext, "modify.png")%>"
											title="Modification" /></td>
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