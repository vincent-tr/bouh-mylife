package org.mylife.home.net.hub.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mylife.home.net.hub.data.DataLink;
import org.mylife.home.net.hub.services.LinkService;
import org.mylife.home.net.hub.services.ServiceAccess;

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
		} else if ("update".equals(action)) {
			update(req, resp);
		} else {
			index(req, resp);
		}
	}

	private void delete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));
		ServiceAccess.getInstance().getLinkService().delete(id);

		resp.sendRedirect(req.getRequestURI());
	}

	private void create(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		DataLink item = new DataLink();
		item.setName(req.getParameter("name"));
		item.setType(req.getParameter("type"));
		item.setAddress(req.getParameter("address"));
		item.setPort(parseIntArg(req.getParameter("port"), 0));
		item.setPassword(req.getParameter("password"));
		ServiceAccess.getInstance().getLinkService().create(item);

		resp.sendRedirect(req.getRequestURI());
	}

	private void update(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		DataLink item = new DataLink();
		item.setId(Integer.parseInt(req.getParameter("id")));
		item.setName(req.getParameter("name"));
		item.setType(req.getParameter("type"));
		item.setAddress(req.getParameter("address"));
		item.setPort(parseIntArg(req.getParameter("port"), 0));
		item.setPassword(req.getParameter("password"));
		ServiceAccess.getInstance().getLinkService().update(item);

		resp.sendRedirect(req.getRequestURI());
	}

	private void index(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// par défaut redirection vers la jsp
		LinkService service = ServiceAccess.getInstance().getLinkService();
		List<DataLink> data = service.list();
		Map<String, String> types = service.listTypes();

		req.setAttribute("data", data);
		req.setAttribute("types", types);
		req.setAttribute("title", "Gestion de la configuration");
		req.getRequestDispatcher("/jsp/Configuration.jsp").forward(req, resp);
	}
	
	private int parseIntArg(String argumentValue, Integer defaultValue) {
		try {
			return Integer.parseInt(argumentValue);
		} catch(NumberFormatException nfe) {
			if(defaultValue == null)
				throw nfe;
			return defaultValue.intValue();
		}
	}
}
