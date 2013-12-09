<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.net.hub.web.model.*"%>
<%
	ServerView server = (ServerView) pageContext.getRequest()
			.getAttribute("server");
%>
<ul>
	<li><img style="height: 16px; width: 16px"
		src="<%=WebTools.image(pageContext, "server.gif")%>" title="Serveur" /><%=WebTools.htmlEscape(server.getName())%></li>
	<ul>
		<%
			for (UserView user : server.getUsers()) {
				StringBuffer channels = new StringBuffer();
				for (String channel : user.getChannels()) {
					if (channels.length() > 0)
						channels.append(", ");
					channels.append(channel);
				}
		%>
		<li><img style="height: 16px; width: 16px"
			src="<%=WebTools.image(pageContext, "user.png")%>"
			title="Utilisateur" /><%=WebTools.htmlEscape(user.getName())%>&nbsp;(Salons
			: <%=channels.toString()%>)</li>
		<%
			}
		%>
		<%
			for (ServerView child : server.getChildren()) {
				pageContext.getRequest().setAttribute("server", child);
		%>
		<jsp:include page="NetworkStateNode.jsp" />
		<%
			}
		%>
	</ul>
</ul>