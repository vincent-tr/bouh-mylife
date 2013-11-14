package org.mylife.home.net.hub.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet de configuration
 * 
 * @author pumbawoman
 * 
 */
@MultipartConfig
public class WebConfiguration extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8986836924739824605L;

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
		if ("delete".equals(action)) {
			delete(req, resp);
		} else if ("create".equals(action)) {
			create(req, resp);
		} else {
			index(req, resp);
		}
	}

	private void delete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));
		//ServiceAccess.getConfigurationService().delete(id);

		resp.sendRedirect(req.getRequestURI());
	}

	private void create(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		/*
		DataConfiguration item = new DataConfiguration();
		item.setType(req.getParameter("type"));
		item.setComment(req.getParameter("comment"));
		item.setContent(readPart(req.getPart("content")));
		ServiceAccess.getConfigurationService().create(item);
		*/
		resp.sendRedirect(req.getRequestURI());
	}
	
	private void index(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		/*
		// par défaut redirection vers la jsp
		List<DataConfiguration> data = ServiceAccess.getConfigurationService()
				.list();
		req.setAttribute("data", data);*/
		req.setAttribute("title", "Gestion de la configuration");
		req.getRequestDispatcher("/jsp/Configuration.jsp").forward(req, resp);
	}
}
