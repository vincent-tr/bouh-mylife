<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.net.*"%>
<%@ page import="org.mylife.home.net.structure.*"%>
<div class="table_render_outer">
	<div class="table_render_inner">
		<%
			for (NetContainer container : NetRepository.getObjects()) {
				NetObject obj = container.getObject();
				NetClass clazz = obj.getNetClass();
		%>
		<table class="table_render">
			<thead>
				<tr>
					<th colspan="3">Id : <%=WebTools.htmlEscape(obj.getId())%>,
						Local : <%=container.isLocal() ? "oui" : "non"%>, Connecté : <%=container.isConnected() ? "oui" : "non"%></th>
				</tr>
				<tr>
					<th width="25%">Nom</th>
					<th width="25%">Type</th>
					<th>Valeur</th>
				</tr>
			</thead>
			<tbody>
				<%
					for (NetMember member : clazz.getMembers()) {
							if (!(member instanceof NetAttribute))
								continue;
							NetAttribute attribute = (NetAttribute) member;
							Object value = obj.getAttributeValue(attribute.getName());
				%>
				<tr>
					<td><%=WebTools.htmlEscape(attribute.getName())%></td>
					<td><%=WebTools.htmlEscape(attribute.getType()
							.toString())%></td>
					<td><%=WebTools.htmlEscape(String.valueOf(value))%></td>
				</tr>
				<%
					}
				%>
			</tbody>
		</table>
		&nbsp;
		<%
			}
		%>
	</div>
</div>