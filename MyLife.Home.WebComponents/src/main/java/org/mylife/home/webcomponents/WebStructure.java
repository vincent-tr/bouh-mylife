package org.mylife.home.webcomponents;

import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.mylife.home.net.exchange.ExchangeManager;
import org.mylife.home.net.exchange.XmlNetContainer;
import org.mylife.home.net.exchange.XmlNetObject;

/**
 * Servlet d'export de structure xml
 * @author trumpffv
 *
 */
public class WebStructure extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1746574994991312453L;

	/**
	 * Exécution de la demande
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Properties meProperties = WebTools.getConfig(this);
		
		String host = InetAddress.getLocalHost().getHostName();
		String name = meProperties.getProperty("name");
		String version = meProperties.getProperty("version");
		String buildTimestamp = meProperties.getProperty("build.timestamp");
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String currentDate = dateFormat.format(cal.getTime());
		
		XmlNetContainer container = new XmlNetContainer();
		container.componentsVersion = name + " " + host + " " + version + " " + buildTimestamp;
		container.documentVersion = currentDate;
		
		List<XmlNetObject> xmlComponents = new ArrayList<XmlNetObject>();
		Collection<Component> components = ComponentRepository.getComponents();
		for(Component component : components) {
			xmlComponents.add(ExchangeManager.marshal(component.getObject()));
		}
		container.components = xmlComponents.toArray(new XmlNetObject[xmlComponents.size()]);
		
		resp.setContentType("application/xml");
		
		try {
			ExchangeManager.exportContainer(container, resp.getOutputStream());
		} catch (JAXBException e) {
			throw new ServletException("error exporting", e);
		}
	}
}
