<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="java.util.Collection"%>
<%@ page import="org.mylife.home.core.links.Link"%>
<%
	Collection<Link> data = (Collection<Link>) pageContext.getRequest()
			.getAttribute("data");
%>
<div class="table_render_outer">
	<div class="table_render_inner">
		<table class="table_render">
			<thead>
				<tr>
					<th>Composant source</th>
					<th>Attribut source</th>
					<th>Source locale</th>
					<th>Source connectée</th>
					<th>Composant cible</th>
					<th>Action cible</th>
					<th>Cible locale</th>
					<th>Cible connectée</th>
					<th>Type de donnée sur le lien</th>
				</tr>
			</thead>
			<tbody>
				<%
					for (Link item : data) {
				%>
				<tr>
					<td><%=WebTools.htmlEscape(item.getSourceContainer()
						.getObject().getId())%></td>
					<td><%=WebTools.htmlEscape(item.getSourceAttributeName())%></td>
					<td><%=item.getSourceContainer().isLocal() ? "oui" : "non"%></td>
					<td><%=item.getSourceContainer().isConnected() ? "oui"
						: "non"%></td>
					<td><%=WebTools.htmlEscape(item.getTargetContainer()
						.getObject().getId())%></td>
					<td><%=WebTools.htmlEscape(item.getTargetMethodName())%></td>
					<td><%=item.getTargetContainer().isLocal() ? "oui" : "non"%></td>
					<td><%=item.getTargetContainer().isConnected() ? "oui"
						: "non"%></td>
					<td><%=WebTools.htmlEscape(item.getLinkType().toString())%></td>
				</tr>
				<%
					}
				%>
			</tbody>
		</table>
	</div>
</div>