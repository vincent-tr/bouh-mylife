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
 * 
 * @author pumbawoman
 * 
 */
public class ConfigurationServlet extends HttpServlet {

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
		} else {
			index(req, resp);
		}
	}
	
	private void comment(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		DataAccess access = new DataAccess();
		try {
			DataConfiguration item = access.getConfigurationByKey(Integer.parseInt(req.getParameter("id")));
			item.setComment(req.getParameter("comment"));
			access.updateConfiguration(item);
		} finally {
			access.close();
		}
		
		resp.sendRedirect(req.getRequestURI());
	}

	private void activate(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		DataAccess access = new DataAccess();
		try {
			DataConfiguration item = access.getConfigurationByKey(Integer.parseInt(req.getParameter("id")));
			item.setActive(true);
			access.updateConfiguration(item);
		} finally {
			access.close();
		}
		
		resp.sendRedirect(req.getRequestURI());
	}

	private void deactivate(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		DataAccess access = new DataAccess();
		try {
			DataConfiguration item = access.getConfigurationByKey(Integer.parseInt(req.getParameter("id")));
			item.setActive(false);
			access.updateConfiguration(item);
		} finally {
			access.close();
		}
		
		resp.sendRedirect(req.getRequestURI());
	}

	private void delete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		DataAccess access = new DataAccess();
		try {
			DataConfiguration item = new DataConfiguration();
			item.setId(Integer.parseInt(req.getParameter("id")));
			access.deleteConfiguration(item);
		} finally {
			access.close();
		}
		
		resp.sendRedirect(req.getRequestURI());
	}

	private void content(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		DataAccess access = new DataAccess();
		try {
			DataConfiguration item = access.getConfigurationByKey(Integer.parseInt(req.getParameter("id")));
			
			resp.setContentType("text/xml");
			
			byte[] content = item.getContent(); 
			resp.getOutputStream().write(content, 0, content.length);
			
		} finally {
			access.close();
		}
	}

	private void index(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// par défaut redirection vers la jsp
		DataAccess access = new DataAccess();
		try {
			Set<DataConfiguration> data = access.getConfigurationsAll();
			req.setAttribute("data", data);
			req.getRequestDispatcher("/jsp/config/List.jsp").forward(req, resp);
		} finally {
			access.close();
		}
	}
}
