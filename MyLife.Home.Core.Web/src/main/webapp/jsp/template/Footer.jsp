<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.Properties" %>    
<%@ page import="org.mylife.home.core.web.WebTools" %>    
     
<%
	Properties meProperties = new Properties();
	meProperties.load(pageContext.getServletContext().getResourceAsStream(
				"/WEB-INF/classes/me.properties"));
	String name = meProperties.getProperty("name");
	String version = meProperties.getProperty("version");
	String buildTimestamp = meProperties.getProperty("build.timestamp");
%>

<br/><br/>
<%= WebTools.htmlEscape(String.format("%s %s (built : %s)", name, version, buildTimestamp)) %>
</body>
</html>
