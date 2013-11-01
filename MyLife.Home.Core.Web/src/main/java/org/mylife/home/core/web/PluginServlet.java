package org.mylife.home.core.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mylife.home.core.data.DataPlugin;
import org.mylife.home.core.services.ServiceAccess;

/**
 * Serlet de gestion des plugins
 * 
 * @author pumbawoman
 * 
 */
public class PluginServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 855594790262258400L;

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

		int id = Integer.parseInt(req.getParameter("id"));
		String comment = req.getParameter("comment");
		ServiceAccess.getPluginService().changeComment(id, comment);

		resp.sendRedirect(req.getRequestURI());
	}

	private void activate(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));
		ServiceAccess.getPluginService().changeActive(id, true);

		resp.sendRedirect(req.getRequestURI());
	}

	private void deactivate(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));
		ServiceAccess.getPluginService().changeActive(id, false);

		resp.sendRedirect(req.getRequestURI());
	}

	private void delete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));
		ServiceAccess.getPluginService().delete(id);

		resp.sendRedirect(req.getRequestURI());
	}

	private void content(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		DataPlugin item = ServiceAccess.getPluginService().get(
				Integer.parseInt(req.getParameter("id")));
		resp.setContentType("text/xml");
		byte[] content = item.getContent();
		resp.getOutputStream().write(content, 0, content.length);
	}

	private void index(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// par défaut redirection vers la jsp
		List<DataPlugin> data = ServiceAccess.getPluginService().list();
		req.setAttribute("data", data);
		req.getRequestDispatcher("/jsp/Plugin.jsp").forward(req, resp);
	}
}
