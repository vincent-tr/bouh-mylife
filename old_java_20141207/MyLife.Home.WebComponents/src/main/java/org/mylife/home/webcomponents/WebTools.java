package org.mylife.home.webcomponents;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServlet;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Outils web
 * @author trumpffv
 *
 */
public class WebTools {


	/**
	 * HTML escape
	 * 
	 * @param input
	 * @return
	 */
	public static String htmlEscape(String input) {
		return StringEscapeUtils.escapeHtml4(input);
	}
	
	/**
	 * Obtention de la configuration
	 * @param servlet
	 * @return
	 * @throws IOException
	 */
	public static Properties getConfig(HttpServlet servlet) throws IOException {
		
		Properties props = new Properties();
		props.load(servlet.getServletContext().getResourceAsStream(
				"/WEB-INF/classes/me.properties"));
		return props;
	}
}
