<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%
	String data = (String) pageContext.getRequest()
			.getAttribute("data");
%>
<table class="form_format">
	<tbody>
		<tr>
			<td>Lien direct :</td>
			<td><input type="text" style="width: 100%;" readonly="readonly"
				value="<%=WebTools.htmlEscape(WebTools.fullCallingRoot(pageContext)
					+ "/structure")%>" /></td>
		</tr>
		<tr>
			<td>Contenu :</td>
			<td>
				<div
					style="position: relative; height: 400px; vertical-align: middle;">
					<div
						style="position: absolute; top: 2px; bottom: 8px; left: 0px; right: 8px;">
						<textarea
							style="resize: none; margin: 0; width: 100%; height: 100%;"
							readonly="readonly"><%=WebTools.htmlEscape(data, false)%></textarea>
					</div>
				</div>
			</td>

		</tr>
	</tbody>
</table>
