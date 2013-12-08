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
					<th width="40%">Adresse distante</th>
					<th width="40%">Serveur distant</th>
					<th>Actions</th>
				</tr>
			</thead>
			<tbody>
				<%
					for (RunningLink link : data) {
				%>
				<tr>
					<td><%=WebTools.htmlEscape(link.getRemoteHost())%></td>
					<td><%=WebTools.htmlEscape(link.getServerName())%></td>
					<td><a
						href="?action=serverClose&server=<%=WebTools.urlEscape(link.getServerName())%>"><img
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