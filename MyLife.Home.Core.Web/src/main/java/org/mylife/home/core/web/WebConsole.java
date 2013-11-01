package org.mylife.home.core.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet console
 * 
 * @author pumbawoman
 *
 */
public class WebConsole extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 445840739835708708L;


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		dispatch(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		dispatch(req, resp);
	}

	private void dispatch(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String action = req.getParameter("action");
		/*
		if ("comment".equals(action)) {
			comment(req, resp);
		} else if ("activate".equals(action)) {
			activate(req, resp);
		} else if ("deactivate".equals(action)) {
			deactivate(req, resp);
		} else if ("delete".equals(action)) {
			delete(req, resp);
		} else if ("content".equals(action)) {
			content(req, resp);
		} else if ("create".equals(action)) {
			create(req, resp);
		} else if ("contentCreate".equals(action)) {
			contentCreate(req, resp);
		} else if ("downloadCreate".equals(action)) {
			downloadCreate(req, resp);
		} else {*/
			index(req, resp);
		//}
	}
	
	private void index(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// par défaut redirection vers la jsp
		//List<DataConfiguration> data = ServiceAccess.getConfigurationService()
		//		.list();
		//req.setAttribute("data", data);
		req.getRequestDispatcher("/jsp/Console.jsp").forward(req, resp);
	}
}
