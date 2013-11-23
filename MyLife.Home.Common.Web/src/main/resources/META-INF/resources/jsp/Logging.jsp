<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Set"%>
<%@ page import="org.mylife.home.common.collections.TreeNode"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.common.web.model.LogItem"%>
<%
	String logger = (String) pageContext.getRequest().getAttribute(
			"logger");
	int minLevel = (Integer) pageContext.getRequest().getAttribute(
			"minLevel");
	int maxLevel = (Integer) pageContext.getRequest().getAttribute(
			"maxLevel");
	int maxCount = (Integer) pageContext.getRequest().getAttribute(
			"maxCount");

	List<LogItem> data = (List<LogItem>) pageContext.getRequest()
			.getAttribute("data");
	List<Integer> maxCountValues = (List<Integer>) pageContext
			.getRequest().getAttribute("maxCountValues");
	Set<String> loggers = (Set<String>) pageContext.getRequest()
			.getAttribute("loggers");
	Map<Integer, String> levels = (Map<Integer, String>) pageContext
			.getRequest().getAttribute("levels");
	boolean showError = (Boolean) pageContext.getRequest()
			.getAttribute("showError");
%>

<%@include file="/jsp/template/Header.jsp"%>


<fieldset>
	<legend>
		<span>Sélection</span>
	</legend>

	<form method="post" action="">

		<table class="form_format">
			<tbody>
				<tr>
					<td>Logger :</td>
					<td><input style="width: 100%;" type="text" name="logger" value="<%=WebTools.htmlEscape(logger)%>" /></td>
				</tr>
				<tr>
					<td>Level min :</td>
					<td><select name="minLevel">
							<%
								for (Map.Entry<Integer, String> level : levels.entrySet()) {
							%>
							<option value="<%=level.getKey()%>"
								<%if (level.getKey().equals(minLevel)) {%> selected="selected"
								<%}%>><%=WebTools.htmlEscape(level.getValue())%></option>
							<%
								}
							%>
					</select></td>
				</tr>
				<tr>
					<td>Level max :</td>
					<td><select name="maxLevel">
							<%
								for (Map.Entry<Integer, String> level : levels.entrySet()) {
							%>
							<option value="<%=level.getKey()%>"
								<%if (level.getKey().equals(maxLevel)) {%> selected="selected"
								<%}%>><%=WebTools.htmlEscape(level.getValue())%></option>
							<%
								}
							%>
					</select></td>
				</tr>
				<tr>
					<td>Nombre de données :</td>
					<td><select name="maxCount">
							<%
								for (Integer value : maxCountValues) {
							%>
							<option value="<%=value%>" <%if (value.equals(maxCount)) {%>
								selected="selected" <%}%>><%=value%></option>
							<%
								}
							%>
					</select></td>
				</tr>
				<tr>
					<td>Afficher les erreurs :</td>
					<td><input type="checkbox" name="showError" value="true"
						<%if (showError) {%> checked="checked" <%}%>></td>
				</tr>
				<tr>
					<td colspan="2" class="form_action"><input type="image"
						src="<%=WebTools.image(pageContext, "view.png")%>"
						title="Sélection" /></td>
				</tr>
			</tbody>
		</table>
	</form>

</fieldset>

<fieldset>
	<legend>
		<span>Données</span>
	</legend>

	<div class="table_render_outer">
		<div class="table_render_inner">
			<table class="table_render">
				<thead>
					<tr>
						<th width="150px">Date</th>
						<th width="120px">Logger</th>
						<th width="60px">ThreadID</th>
						<th width="60px">Level</th>
						<th>Message</th>
						<%
							if (showError) {
						%>
						<th>Error</th>
						<%
							}
						%>
					</tr>
				</thead>
				<tbody>
					<%
						for (LogItem item : data) {
					%>
					<tr>
						<td><span
							style="color: <%=WebTools.severityColor(item.getSeverity())%>"><%=WebTools.formatDate(item.getDate())%></span></td>
						<td style="text-align: left;"><span
							style="color: <%=WebTools.severityColor(item.getSeverity())%>"><%=WebTools.htmlEscape(item.getLogger())%></span></td>
						<td><span
							style="color: <%=WebTools.severityColor(item.getSeverity())%>"><%=WebTools.htmlEscape("" + item.getThreadId())%></span></td>
						<td><span
							style="color: <%=WebTools.severityColor(item.getSeverity())%>"><%=WebTools.htmlEscape(item.getLevel())%></span></td>
						<td style="text-align: left;"><span
							style="color: <%=WebTools.severityColor(item.getSeverity())%>"><%=WebTools.htmlEscape(item.getMessage())%></span></td>
						<%
							if (showError) {
						%>
						<td style="text-align: left;"><span
							style="color: <%=WebTools.severityColor(item.getSeverity())%>"><%=WebTools.htmlEscape(item.getError())%></span></td>
						<%
							}
						%>
					</tr>
					<%
						}
					%>
				</tbody>
			</table>
		</div>
	</div>
</fieldset>

<%@include file="/jsp/template/Footer.jsp"%>