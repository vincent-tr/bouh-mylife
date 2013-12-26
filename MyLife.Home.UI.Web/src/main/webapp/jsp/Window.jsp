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
<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"></script>

<script>
	$(function() {
		if (!("WebSocket" in window)) {
			alert("WebSocket NOT supported !");
			return;
		}
		
		window.netEndpoint = new WebSocket('<%=WebUiTools.webSocketUrl(pageContext)%>');
		window.netEndpoint.onopen = function() {
			console.log('netEndpoint opened');
			window.netEndpoint.sendMessage('window <%=window.getId()%>'); /* TODO : panels enfants */
		};
		window.netEndpoint.onmessage = function(evt) {
			var msg = evt.data;
			console.log('netEndpoint receive : ' + msg);
			/* TODO */
		};
		window.netEndpoint.onclose = function() {
			console.log('netEndpoint closed');
		};
		window.netEndpoint.sendMessage = function(msg) {
			console.log('netEndpoint send : ' + msg);
			window.netEndpoint.send(msg);
		};
	});
</script>
</head>
<body>
	<jsp:include page="Panel.jsp" />
</body>
</html>
