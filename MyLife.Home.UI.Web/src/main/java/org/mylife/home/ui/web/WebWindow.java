package org.mylife.home.ui.web;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.mylife.home.ui.services.DispatcherService;
import org.mylife.home.ui.structure.Structure;
import org.mylife.home.ui.structure.Window;

public class WebWindow extends HttpServlet {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(WebWindow.class
			.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 5617676896400025611L;

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

		Window window = findWindow(req);
		
		req.setAttribute("window", window);
		req.getRequestDispatcher("/jsp/Window.jsp").forward(req, resp);
	}

	private Window findWindow(HttpServletRequest req) {
		String id = req.getParameter("id");
		Window window = null;
		if (!StringUtils.isEmpty(id)) {
			window = Structure.getWindow(id);
			if (window == null)
				log.warning("Unknown window, returning default : " + id);
		}
		if (window == null)
			window = Structure.getDefaultWindow();
		return window;
	}
}
