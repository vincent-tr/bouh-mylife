<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.ui.web.WebUiTools"%>
<%@ page import="org.mylife.home.ui.structure.*"%>
<%@ page import="org.apache.commons.lang3.*"%>

<%
	Window window = (Window) pageContext.getRequest().getAttribute(
	"window");
%>

<div id="panelPopup">
	<div id="panelLoader"></div>
</div>

<div id="<%=window.getId()%>">
	<%
		String backgroundId = window.getBackgroundId();
		if (!StringUtils.isEmpty(backgroundId)) {
	%>
	<img src="<%=WebUiTools.image(pageContext, backgroundId)%>" />
	<%
		}
	%>
	<%
		for (Component component : window.getComponents()) {
	%>
	<div id="<%=window.getId()%>:<%=component.getId()%>">
		<!-- onclic -->
		<!-- imageSelection -->
		<%
			for (String iconId : component.getIcons()) {
					if (StringUtils.isEmpty(iconId))
						continue;
		%>
		<img src="<%=WebUiTools.image(pageContext, iconId)%>" hidden />
		<%
			}
		%>
	</div>
	<%
		}
	%>
</div>

<script>
	$(function() {
		$("#panelPopup").dialog({
			autoOpen : false
		});
		/*
		var url = '<%=WebTools.servlet(pageContext, "console")%>';
		url += '?action=panel&id=toto.id';
		$('#panelLoader').load(url);
		 */
		/*
		$("#opener").click(function() {
			$("#dialog").dialog("open");
		});
		 */
	});
</script>
