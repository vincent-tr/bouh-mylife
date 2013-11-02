<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="org.mylife.home.core.web.WebTools"%>

<%@include file="/jsp/template/Header.jsp"%>

<div class="tabs">
	<table>
		<tr>
			<td>
				<ul>
					<li><a href="#tabs-1"><img
							src="<%=WebTools.image(pageContext, "view.png")%>" />Etat du
							serveur</a></li>
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
			</td>
		</tr>
	</table>
</div>

<script>
$('#serverState').load('<%= WebTools.servlet(pageContext, "console") %>?action=serverState');
self.setInterval(function() {
	$('#serverState').load('<%= WebTools.servlet(pageContext, "console") %>?action=serverState');
}, 5000);
</script>

<%@include file="/jsp/template/Footer.jsp"%>