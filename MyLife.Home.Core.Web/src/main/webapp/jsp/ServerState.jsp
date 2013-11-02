<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.Date"%>
<%@ page import="org.mylife.home.core.web.WebTools"%>
<%@ page import="org.mylife.home.core.web.model.ServerState"%>
<%@ page import="org.mylife.home.core.web.model.Severity"%>
<%
	ServerState data = (ServerState) pageContext.getRequest()
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
			<td><span style="color: <%=WebTools.severityColor(Severity.ERROR)%>"><%=WebTools.formatError(data.getError())%></span></td>
		</tr>
	</tbody>
</table>
