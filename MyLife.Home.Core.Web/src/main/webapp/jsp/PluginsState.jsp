<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Map"%>
<%@ page import="org.mylife.home.core.plugins.PluginView"%>
<%@ page import="org.mylife.home.net.NetObject"%>
<%
	Collection<PluginView> data = (Collection<PluginView>) pageContext
			.getRequest().getAttribute("data");
%>
<div class="table_render_outer">
	<div class="table_render_inner">
		<%
			for (PluginView item : data) {
		%>
		<table class="table_render">
			<thead>
				<tr>
					<th colspan="2">Id : <%=WebTools.htmlEscape(item.getId())%> (UI : <%=item.isUi() ? "oui" : "non" %>)</th>
				</tr>
				<tr>
					<th colspan="2">Caractéristiques :</th>
				</tr>
				<tr>
					<th style="width: 25%;">Nom</th>
					<th>Valeur</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>Type de plugin</td>
					<td><%=WebTools.htmlEscape(item.getType())%></td>
				</tr>
				<tr>
					<td>Type de plugin : affichage</td>
					<td><%=WebTools.htmlEscape(item.getDisplayType())%></td>
				</tr>
				<%
					for (NetObject netObject : item.getPublishedObjects()) {
				%>
				<tr>
					<td>Objet publié</td>
					<td><%=WebTools.htmlEscape(netObject.getId())%></td>
				</tr>
				<%
					}
				%>
			</tbody>
			<thead>
				<tr>
					<th colspan="2">Configuration :</th>
				</tr>
				<tr>
					<th style="width: 25%;">Nom</th>
					<th>Valeur</th>
				</tr>
			</thead>
			<tbody>
				<%
					Map<String, String> configuration = item.getConfiguration();
						for (Map.Entry<String, String> configItem : configuration
								.entrySet()) {
				%>
				<tr>
					<td><%=WebTools.htmlEscape(configItem.getKey())%></td>
					<td><%=WebTools.htmlEscape(configItem.getValue())%></td>
				</tr>
				<%
					}
				%>
			</tbody>
		</table>
		&nbsp;
		<%
			}
		%>
	</div>
</div>