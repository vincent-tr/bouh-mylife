<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.Date"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.net.hub.irc.*"%>
<%@ page
	import="org.mylife.home.net.hub.services.LinkService.RunningLink"%>
<%
	Set<RunningLink> data = (Set<RunningLink>) pageContext.getRequest()
			.getAttribute("data");
%>

<div class="table_render_outer">
	<div class="table_render_inner">
		<%
			if (data == null) {
		%>
		No data available
		<%
			} else {
		%>
		<table class="table_render">
			<thead>
				<tr>
					<th width="25%">Adresse distante</th>
					<th width="25%">Serveur distant</th>
					<th>Octets envoyés</th>
					<th>Octets reçus</th>
					<th>Lignes envoyées</th>
					<th>Lignes reçues</th>
					<th>Heure d'établissement</th>
					<th>Actions</th>
				</tr>
			</thead>
			<tbody>
				<%
					for (RunningLink link : data) {
							Connection con = link.getConnection();
							Server server = link.getServer();
				%>
				<tr>
					<td><%=WebTools.htmlEscape(con.getRemoteAddress() + ":"
							+ con.getRemotePort())%></td>
					<td><%=WebTools.htmlEscape(server.getName())%></td>
					<td><%=WebTools.htmlEscape(String.valueOf(con
							.getBytesSent()))%></td>
					<td><%=WebTools.htmlEscape(String.valueOf(con
							.getBytesRead()))%></td>
					<td><%=WebTools.htmlEscape(String.valueOf(con
							.getLinesSent()))%></td>
					<td><%=WebTools.htmlEscape(String.valueOf(con
							.getLinesRead()))%></td>
					<td><%=WebTools.formatDate(new Date(con
							.getConnectTimeMillis()))%></td>
					<td><a
						href="?action=linksClose&server=<%=WebTools.urlEscape(server.getName())%>"><img
							src="<%=WebTools.image(pageContext, "erase.png")%>"
							title="Supprimer" /></a></td>
				</tr>
				<%
					}
				%>
			</tbody>
		</table>
		<%
			}
		%>
	</div>
</div>