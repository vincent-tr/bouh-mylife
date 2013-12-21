<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Collection"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.core.plugins.PluginFactory"%>

<%@include file="/jsp/template/Header.jsp"%>
<%
	Collection<PluginFactory> factories = (Collection<PluginFactory>)pageContext.getRequest().getAttribute("factories");
%>

<script>
	$(function() {
		$(".tabs").tabs();
	});
</script>

<div class="tabs">
	<table>
		<tr>
			<td>
				<ul class="tabHeaders">
					<li><a href="#tabs-1"><img
							src="<%=WebTools.image(pageContext, "view.png")%>" />Etat du
							serveur</a></li>
					<li><a href="#tabs-2"><img
							src="<%=WebTools.image(pageContext, "application.png")%>" />Fabriques
							de plugins</a></li>
					<li><a href="#tabs-3"><img
							src="<%=WebTools.image(pageContext, "text_preview.png")%>" />Structure
							de designer de plugins</a></li>
					<li><a href="#tabs-4"><img
							src="<%=WebTools.image(pageContext, "application.png")%>" />Vue
							des composants</a></li>
					<li><a href="#tabs-5"><img
							src="<%=WebTools.image(pageContext, "application.png")%>" />Vue
							des plugins</a></li>
					<li><a href="#tabs-6"><img
							src="<%=WebTools.image(pageContext, "lightning.png")%>" />Vue
							des liens</a></li>
				</ul>
			</td>
		</tr>
		<tr>
			<td>
				<div id="tabs-1">
					<fieldset>
						<legend>
							<span>Etat du serveur</span>
						</legend>

						<div id="serverState"></div>

					</fieldset>
				</div>
				<div id="tabs-2">
					<fieldset>
						<legend>
							<span>Fabriques de plugins</span>
						</legend>

						<div class="table_render_outer">
							<div class="table_render_inner">
								<table class="table_render">
									<thead>
										<tr>
											<td>Type de plugin</td>
											<td>Type de plugin (affichage)</td>
											<td>UI</td>
											<td>Classe d'implémentation de la fabrique</td>
										</tr>
									</thead>
									<tbody>
										<%
											for(PluginFactory factory : factories) {
										%>
										<tr>
											<td><%=WebTools.htmlEscape(factory.getType())%></td>
											<td><%=WebTools.htmlEscape(factory.getDisplayType())%></td>
											<td><%=factory.getDesignMetadata().isUi() ? "oui" : "non"%></td>
											<td><%=WebTools.htmlEscape(factory.getClass().toString())%></td>
										</tr>
										<%
											}
										%>
									</tbody>
								</table>
							</div>
						</div>
					</fieldset>
				</div>
				<div id="tabs-3">
					<fieldset>
						<legend>
							<span>Structure de designer de plugins</span>
						</legend>

						<div id="designStructureState"></div>

					</fieldset>
				</div>
				<div id="tabs-4">
					<fieldset>
						<legend>
							<span>Vue des composants</span>
						</legend>

						<div id="componentsState"></div>

					</fieldset>
				</div>
				<div id="tabs-5">
					<fieldset>
						<legend>
							<span>Vue des plugins</span>
						</legend>

						<div id="pluginsState"></div>
					</fieldset>
				</div>
				<div id="tabs-6">
					<fieldset>
						<legend>
							<span>Vue des liens</span>
						</legend>

						<div id="linksState"></div>
					</fieldset>
				</div>
			</td>
		</tr>
	</table>
</div>

<script>
$('#serverState').load('<%=WebTools.servlet(pageContext, "console")%>?action=serverState');
$('#componentsState').load('<%=WebTools.servlet(pageContext, "console")%>?action=componentsState');
$('#pluginsState').load('<%=WebTools.servlet(pageContext, "console")%>?action=pluginsState');
$('#linksState').load('<%=WebTools.servlet(pageContext, "console")%>?action=linksState');
$('#designStructureState').load('<%=WebTools.servlet(pageContext, "console")%>?action=designStructureState');
self.setInterval(function() {
	$('#serverState').load('<%=WebTools.servlet(pageContext, "console")%>?action=serverState');
	$('#componentsState').load('<%=WebTools.servlet(pageContext, "console")%>?action=componentsState');
	/* pas de refresh car pas de modification une fois en ligne */
	/* $('#pluginsState').load('<%=WebTools.servlet(pageContext, "console")%>?action=pluginsState'); */
	/* $('#designStructureState').load('<%=WebTools.servlet(pageContext, "console")%>?action=designStructureState'); */
	$('#linksState').load('<%=WebTools.servlet(pageContext, "console")%>
	?action=linksState');
					}, 5000);
</script>

<%@include file="/jsp/template/Footer.jsp"%>