<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.net.*"%>
<%@ page import="org.mylife.home.net.structure.*"%>
<%!/**
	 * Représentation d'un type
	 * @param type
	 * @return
	 */
	private String getTypeDisplay(NetType type) {
		if (type instanceof NetRange) {
			NetRange range = (NetRange) type;
			return String.format("Range[%d..%d]", range.getMin(),
					range.getMax());
		} else if (type instanceof NetEnum) {
			NetEnum enu = (NetEnum) type;
			StringBuffer buffer = new StringBuffer();
			buffer.append("Enum[");
			boolean first = true;
			for (String value : enu.getValues()) {
				if (first)
					first = false;
				else
					buffer.append(",");
				buffer.append(value);
			}
			buffer.append("]");
			return buffer.toString();
		} else {
			throw new UnsupportedOperationException("unknown type");
		}
	}%>
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
					<td><%=WebTools.htmlEscape(getTypeDisplay(attribute
							.getType()))%></td>
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