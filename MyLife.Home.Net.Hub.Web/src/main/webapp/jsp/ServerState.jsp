<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.Date"%>
<%@ page import="org.apache.commons.lang3.StringUtils"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.net.hub.web.model.IrcServerState"%>
<%@ page import="org.mylife.home.common.web.model.Severity"%>
<%
	IrcServerState data = (IrcServerState) pageContext.getRequest()
			.getAttribute("data");
%>

<table class="form_format">
	<tbody>
		<tr>
			<td>Etat du serveur :</td>
			<td><img
				src="<%=WebTools.severityImage(pageContext, data.getSeverity())%>" />&nbsp;<span
				style="color: <%=WebTools.severityColor(data.getSeverity())%>"><%=WebTools.htmlEscape(data.getState())%></span></td>
		</tr>
		<tr>
			<td>Date du statut :</td>
			<td><%=WebTools.formatDate(new Date())%></td>
		</tr>
		<tr>
			<td>Actions :</td>
			<td><a
				href="<%=data.isCanStart() ? (WebTools.servlet(pageContext,
					"console") + "?action=start") : "javascript:void(0)"%>"
				title="Démarrage"><img
					src="<%=WebTools.image(pageContext,
					data.isCanStart() ? "play.png" : "play_disabled.png")%>" /></a>&nbsp;&nbsp;&nbsp;<a
				href="<%=data.isCanStop() ? (WebTools.servlet(pageContext,
					"console") + "?action=stop") : "javascript:void(0)"%>"
				title="Arrêt"><img
					src="<%=WebTools.image(pageContext, data.isCanStop() ? "stop.png"
					: "stop_disabled.png")%>" /></a></td>
		</tr>
		<tr>
			<td>Dernière erreur :</td>
			<td><span
				style="color: <%=WebTools.severityColor(Severity.ERROR)%>"><%=WebTools.formatError(data.getError())%></span></td>
		</tr>
		<%
			if (data.isIrcServer()) {
		%>
		<tr>
			<td>IRC server name :</td>
			<td><%=WebTools.htmlEscape(data.getIrcServerName())%></td>
		</tr>
		<tr>
			<td>IRC network name :</td>
			<td><%=WebTools.htmlEscape(data.getIrcNetworkName())%></td>
		</tr>
		<%
			for (String binding : data.getIrcBindings()) {
		%>
		<tr>
			<td>IRC binding :</td>
			<td><%=WebTools.htmlEscape(binding)%></td>
		</tr>
		<%
			}
		%>
		<tr>
			<td>IRC operators :</td>
			<td><%=WebTools.htmlEscape(data.getIrcOperators())%></td>
		</tr>
		<%
			}
		%>
	</tbody>
</table>
