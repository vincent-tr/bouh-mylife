package org.mylife.home.webcomponents;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mylife.home.net.NetObject;
import org.mylife.home.net.structure.NetAction;
import org.mylife.home.net.structure.NetAttribute;
import org.mylife.home.net.structure.NetEnum;
import org.mylife.home.net.structure.NetMember;
import org.mylife.home.net.structure.NetRange;
import org.mylife.home.net.structure.NetType;

/**
 * Servlet de reporting web
 * 
 * @author pumbawoman
 * 
 */
public class WebReport extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7893232454183432915L;
	
	/**
	 * Représentation d'un type
	 * @param type
	 * @return
	 */
	private String getTypeDisplay(NetType type) {
		if(type instanceof NetRange) {
			NetRange range = (NetRange)type;
			return String.format("Range[%d..%d]", range.getMin(), range.getMax());
		}
		else if(type instanceof NetEnum) {
			NetEnum enu = (NetEnum)type;
			StringBuffer buffer = new StringBuffer();
			buffer.append("Enum[");
			boolean first = true;
			for(String value : enu.getValues()) {
				if(first)
					first = false;
				else
					buffer.append(",");
				buffer.append(value);
			}
			buffer.append("]");
			return buffer.toString();
		}
		else {
			throw new UnsupportedOperationException("unknown type");
		}
	}
	
	/**
	 * Représentation de plusieurs types
	 * @param types
	 * @return
	 */
	private String getTypesDisplay(Iterable<NetType> types) {
		boolean first = true;
		StringBuffer buffer = new StringBuffer();
		for(NetType type : types) {
			if(first)
				first = false;
			else
				buffer.append(", ");
			buffer.append(getTypeDisplay(type));
		}
		if(first) {
			// aucun type
			buffer.append("<none>");
		}
		return buffer.toString();
	}

	/**
	 * Exécution de la demande
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Properties meProperties = WebTools.getConfig(this);

		String name = meProperties.getProperty("name");
		String version = meProperties.getProperty("version");
		String buildTimestamp = meProperties.getProperty("build.timestamp");
		Collection<Component> components = ComponentRepository.getComponents();

		PrintWriter out = resp.getWriter();
		out.println("<html><head />");
		out.println("<body>");
		out.println("	<h1>MyLife.Home.WebComponents</h1>");
		out.println("	<h2>Component List</h2>");
		out.println("	<a href=\"" + req.getContextPath() + "/WebStructure\">[xml data]</a>");
		out.println("	<ul>");
		for (Component component : components) {
			out.println("		<li><a href=\"#"
					+ WebTools.htmlEscape(component.getServletName()) + "\">"
					+ WebTools.htmlEscape(component.getServletName()) + "</a></li>");
		}
		out.println("	</ul>");
		for (Component component : components) {
			ServletConfig config = component.getServletConfig();
			NetObject object = component.getObject();
			List<NetMember> members = object.getNetClass()
					.getMembers();
			out.println("	<a name=\"" + WebTools.htmlEscape(component.getServletName())
					+ "\"><h2>" + WebTools.htmlEscape(component.getServletName())
					+ " : " + WebTools.htmlEscape(component.getClass().toString())
					+ "</h2></a>");
			out.println("	<h3>Servlet Config</h3>");
			out.println("	<table border=\"1\">");
			out.println("		<tr>");
			out.println("			<th>Name</th>");
			out.println("			<th>Value</th>");
			out.println("		</tr>");
			for (Enumeration<String> e = config.getInitParameterNames(); e
					.hasMoreElements();) {
				String configName = e.nextElement();
				String configValue = config.getInitParameter(configName);
				out.println("		<tr>");
				out.println("			<td>" + WebTools.htmlEscape(configName) + "</td>");
				out.println("			<td>" + WebTools.htmlEscape(configValue) + "</td>");
				out.println("		</tr>");
			}
			out.println("	</table>");
			out.println("	<h3>Component view</h3>");
			out.println("	<h4>Attributes</h4>");
			out.println("	<table border=\"1\">");
			out.println("		<tr>");
			out.println("			<th>Index</th>");
			out.println("			<th>Name</th>");
			out.println("			<th>Value</th>");
			out.println("			<th>Type</th>");
			out.println("		</tr>");
			for (NetMember member : members) {
				if (!(member instanceof NetAttribute))
					continue;
				NetAttribute attribute = (NetAttribute) member;
				Object value = object.getAttributeValue(attribute.getName());
				out.println("		<tr>");
				out.println("			<td>" + attribute.getIndex() + "</td>");
				out.println("			<td>" + WebTools.htmlEscape(attribute.getName()) + "</td>");
				out.println("			<td>" + WebTools.htmlEscape(String.valueOf(value)) + "</td>");
				out.println("			<td>" + WebTools.htmlEscape(getTypeDisplay(attribute.getType())) + "</td>");
				out.println("		</tr>");
			}
			out.println("	</table>");
			out.println("	<h4>Methods</h4>");
			out.println("	<table border=\"1\">");
			out.println("		<tr>");
			out.println("			<th>Index</th>");
			out.println("			<th>Name</th>");
			out.println("			<th>Argument Types</th>");
			out.println("		</tr>");
			for (NetMember member : members) {
				if (!(member instanceof NetAction))
					continue;
				NetAction action = (NetAction) member;
				out.println("		<tr>");
				out.println("			<td>" + action.getIndex() + "</td>");
				out.println("			<td>" + WebTools.htmlEscape(action.getName()) + "</td>");
				out.println("			<td>" + WebTools.htmlEscape(getTypesDisplay(action.getArguments())) + "</td>");
				out.println("		</tr>");
			}
			out.println("	</table>");
			out.println("	<a href=\"#top\">[top]</a>");
		}
		out.println("<br/><br/>");
		out.println(WebTools.htmlEscape(String.format("%s %s (built : %s)", name, version, buildTimestamp)));
		out.println("</body></html>");
	}
}
