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
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>MyLife.Home.UI.Web</title>
</head>
<body>

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

</body>
</html>