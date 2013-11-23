<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.common.web.model.LogItem"%>
<%
	List<LogItem> data = (List<LogItem>) pageContext.getRequest()
			.getAttribute("data");
%>

<%@include file="/jsp/template/Header.jsp"%>


<fieldset>
	<legend>
		<span>Sélection</span>
	</legend>

	<form method="post" action="?action=create"
		enctype="multipart/form-data">

		<table class="form_format">
			<tbody>
				<tr>
					<td>Nom :</td>
					<td><input style="width: 100%;" type="text" required
						name="name" /></td>
				</tr>
				<tr>
					<td>Type :</td>
					<td></td>
				</tr>
				<tr>
					<td>Adresse :</td>
					<td><input style="width: 100%;" type="text" required
						name="address" /></td>
				</tr>
				<tr>
					<td>Port :</td>
					<td><input style="width: 100%;" type="number" min="1"
						max="65535" required name="port" /></td>
				</tr>
				<tr>
					<td>Mot de passe :</td>
					<td><input style="width: 100%;" type="text" required
						name="password" /></td>
				</tr>
				<tr>
					<td colspan="2" class="form_action"><input type="image"
						src="<%=WebTools.image(pageContext, "view.png")%>"
						title="Sélection" /></td>
				</tr>
			</tbody>
		</table>
	</form>

</fieldset>

<fieldset>
	<legend>
		<span>Données</span>
	</legend>

	<div class="table_render_outer">
		<div class="table_render_inner">
			<table class="table_render">
				<thead>
					<tr>
						<th>Message</th>
						<th width="120px">Type</th>
						<th>Adresse</th>
						<th width="120px">Port</th>
						<th>Mot de passe</th>
						<th width="60px">Actions</th>
					</tr>
				</thead>
				<tbody>
					<%
						for (LogItem item : data) {
					%>
					<tr>
						<td><%=WebTools.htmlEscape(item.getMessage())%></td>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
					</tr>
					<%
						}
					%>
				</tbody>
			</table>
		</div>
	</div>
</fieldset>

<%@include file="/jsp/template/Footer.jsp"%>