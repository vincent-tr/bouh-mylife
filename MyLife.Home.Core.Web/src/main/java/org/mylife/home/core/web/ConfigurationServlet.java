package org.mylife.home.core.web;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mylife.home.core.data.DataAccess;
import org.mylife.home.core.data.DataConfiguration;

/**
 * Servlet de configuration
 * @author pumbawoman
 *
 */
public class ConfigurationServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8986836924739824605L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		// par défaut redirection vers la jsp
		DataAccess access = new DataAccess();
		try {
			Set<DataConfiguration> data = access.getConfigurationsAll();
			req.setAttribute("data", data);
			req.getRequestDispatcher("/jsp/config/List.jsp").forward(req, resp);
		}
		finally {
			access.close();
		}
	}
}
