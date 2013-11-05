<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="org.mylife.home.core.web.WebTools"%>

<%@include file="/jsp/template/Header.jsp"%>

<script>
	$(function() {
		$(".tabs").tabs();
	});
</script>

<div class="tabs">
	<table>
		<tr>
			<td>
				<ul>
					<li><a href="#tabs-1"><img
							src="<%=WebTools.image(pageContext, "view.png")%>" />Etat du
							serveur</a></li>
					<li><a href="#tabs-2"><img
							src="<%=WebTools.image(pageContext, "application.png")%>" />Vue
							des composants</a></li>
					<li><a href="#tabs-3"><img
							src="<%=WebTools.image(pageContext, "application.png")%>" />Vue
							des plugins</a></li>
					<li><a href="#tabs-4"><img
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
							<span>Vue des composants</span>
						</legend>

						<div id="componentsState"></div>

					</fieldset>
				</div>
				<div id="tabs-3">
					<fieldset>
						<legend>
							<span>Vue des plugins</span>
						</legend>

						<div class="table_render_outer">
							<div class="table_render_inner">
								<table class="table_render">
									<thead></thead>
									<tbody></tbody>
								</table>
							</div>
						</div>
					</fieldset>
				</div>
				<div id="tabs-4">
					<fieldset>
						<legend>
							<span>Vue des liens</span>
						</legend>

						<div class="table_render_outer">
							<div class="table_render_inner">
								<table class="table_render">
									<thead></thead>
									<tbody></tbody>
								</table>
							</div>
						</div>
					</fieldset>
				</div>
			</td>
		</tr>
	</table>
</div>

<script>
$('#serverState').load('<%=WebTools.servlet(pageContext, "console")%>?action=serverState');
$('#componentsState').load('<%=WebTools.servlet(pageContext, "console")%>?action=componentsState');
self.setInterval(function() {
	$('#serverState').load('<%=WebTools.servlet(pageContext, "console")%>?action=serverState');
	$('#componentsState').load('<%=WebTools.servlet(pageContext, "console")%>?action=componentsState');
					}, 5000);
</script>

<%@include file="/jsp/template/Footer.jsp"%>