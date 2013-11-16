package org.mylife.home.core.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.mylife.home.core.data.DataConfiguration;
import org.mylife.home.core.services.ServiceAccess;

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
		} else {
			index(req, resp);
		}
	}

	private void comment(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));
		String comment = req.getParameter("comment");
		ServiceAccess.getInstance().getConfigurationService().changeComment(id, comment);

		resp.sendRedirect(req.getRequestURI());
	}

	private void activate(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));
		ServiceAccess.getInstance().getConfigurationService().changeActive(id, true);

		resp.sendRedirect(req.getRequestURI());
	}

	private void deactivate(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));
		ServiceAccess.getInstance().getConfigurationService().changeActive(id, false);

		resp.sendRedirect(req.getRequestURI());
	}

	private void delete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));
		ServiceAccess.getInstance().getConfigurationService().delete(id);

		resp.sendRedirect(req.getRequestURI());
	}

	private void content(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		DataConfiguration item = ServiceAccess.getInstance().getConfigurationService().get(
				Integer.parseInt(req.getParameter("id")));
		resp.setContentType("application/xml");
		byte[] content = item.getContent();
		resp.getOutputStream().write(content, 0, content.length);
	}

	private void create(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		DataConfiguration item = new DataConfiguration();
		item.setType(req.getParameter("type"));
		item.setComment(req.getParameter("comment"));
		item.setContent(readPart(req.getPart("content")));
		ServiceAccess.getInstance().getConfigurationService().create(item);
		
		resp.sendRedirect(req.getRequestURI());
	}

	private void contentCreate(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		byte[] data = readPart(req.getPart("content"));
		ServiceAccess.getInstance().getConfigurationService().createFromContents(data);
		
		resp.sendRedirect(req.getRequestURI());
	}

	private void downloadCreate(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String url = req.getParameter("url");
		ServiceAccess.getInstance().getConfigurationService().createFromContentsUrl(url);
		
		resp.sendRedirect(req.getRequestURI());
	}
	
	private void index(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// par défaut redirection vers la jsp
		List<DataConfiguration> data = ServiceAccess.getInstance().getConfigurationService()
				.list();
		req.setAttribute("data", data);
		req.setAttribute("title", "Gestion des configurations");
		req.getRequestDispatcher("/jsp/Configuration.jsp").forward(req, resp);
	}
	
	private byte[] readPart(Part part) throws IOException {
		return IOUtils.toByteArray(part.getInputStream());
	}
}
