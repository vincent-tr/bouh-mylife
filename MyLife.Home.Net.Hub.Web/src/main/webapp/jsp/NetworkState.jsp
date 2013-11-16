<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.net.hub.irc.*"%>
<%
	Network data = (Network) pageContext.getRequest()
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
					<th colspan="4">Servers</th>
				</tr>
				<tr>
					<th width="25%">Name</th>
					<th width="10%">Token</th>
					<th>Description</th>
					<th width="10%">Users count</th>
				</tr>
			</thead>
			<tbody>
				<%
					for (Server server : data.servers.values()) {
				%>
				<tr>
					<td><%=WebTools.htmlEscape(server.getName())%></td>
					<td><%=WebTools.htmlEscape(String.valueOf(server.getToken()))%></td>
					<td><%=WebTools.htmlEscape(server.getDescription())%></td>
					<td><%=WebTools.htmlEscape(String.valueOf(server.getUsers().size()))%></td>
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
					<th colspan="4">Users</th>
				</tr>
				<tr>
					<th width="20%">Nick</th>
					<th width="20%">Host</th>
					<th width="20%">Server</th>
					<th>Channels</th>
				</tr>
			</thead>
			<tbody>
				<%
					for (Server server : data.servers.values()) {
						for(User user : server.getUsers()) {
							StringBuffer channels = new StringBuffer();
							for(Channel chan : user.getChannels()) {
								if(channels.length() > 0)
									channels.append(", ");
								channels.append(chan.getName());
							}
				%>
				<tr>
					<td><%=WebTools.htmlEscape(user.getNick())%></td>
					<td><%=WebTools.htmlEscape(user.getHostName())%></td>
					<td><%=WebTools.htmlEscape(server.getName())%></td>
					<td><%=WebTools.htmlEscape(channels.toString())%></td>
				</tr>
				<%
						}
					}
				%>
			</tbody>
		</table>
		&nbsp;
			
		<table class="table_render">
			<thead>
				<tr>
					<th colspan="4">Channels</th>
				</tr>
				<tr>
					<th width="25%">Name</th>
					<th>Users</th>
				</tr>
			</thead>
			<tbody>
				<%
					for (Channel channel : data.channels.values()) {
						StringBuffer users = new StringBuffer();
						for(User user : channel.getUsers()) {
							if(users.length() > 0)
								users.append(", ");
							users.append(user.getNick());
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
<%
	}
%>
	</div>
</div>