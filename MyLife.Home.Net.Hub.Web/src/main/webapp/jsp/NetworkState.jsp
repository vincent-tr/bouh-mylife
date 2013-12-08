<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.net.hub.web.model.*"%>
<%
	NetworkView data = (NetworkView) pageContext.getRequest()
			.getAttribute("data");
%>

<div class="table_render_outer">
	<div class="table_render_inner">
<%
	if(data == null) {
%>
	No data available
<%
	} else {
%>

		<table class="table_render">
			<thead>
				<tr>
					<th colspan="2">Local users</th>
				</tr>
				<tr>
					<th width="80%">Name</th>
				</tr>
			</thead>
			<tbody>
				<%
					for (UserView user : data.getLocalServer().getUsers()) {
				%>
				<tr>
					<td><%=WebTools.htmlEscape(user.getName())%></td>
					<td><a
						href="?action=userClose&user=<%=WebTools.urlEscape(user.getName())%>"><img
							src="<%=WebTools.image(pageContext, "erase.png")%>"
							title="Supprimer" /></a></td>
				</tr>
				<%
					}
				%>
			</tbody>
		</table>
		&nbsp;
		
		<table class="table_render">
			<thead>
				<tr>
					<th colspan="2">Channels</th>
				</tr>
				<tr>
					<th width="20%">Name</th>
					<th>Users</th>
				</tr>
			</thead>
			<tbody>
				<%
					for (ChannelView channel : data.getChannels()) {
						StringBuffer users = new StringBuffer();
						for(String user : channel.getUsers()) {
							if(users.length() > 0)
								users.append(", ");
							users.append(user);
						}
				%>
				<tr>
					<td><%=WebTools.htmlEscape(channel.getName())%></td>
					<td><%=WebTools.htmlEscape(users.toString())%></td>
				</tr>
				<%
					}
				%>
			</tbody>
		</table>
		&nbsp;
		
		<table class="table_render">
			<thead>
				<tr>
					<th colspan="2">Map</th>
				</tr>
				<tr>
					<th width="20%">Name</th>
					<th>Users</th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
<%
	}
%>
	</div>
</div>