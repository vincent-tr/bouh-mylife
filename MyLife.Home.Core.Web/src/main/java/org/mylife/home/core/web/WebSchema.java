package org.mylife.home.core.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mylife.home.core.exchange.ExchangeManager;

/**
 * Export des schémas
 * 
 * @author pumbawoman
 * 
 */
public class WebSchema extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6306178097985223985L;


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
		if ("core".equals(action)) {
			core(req, resp);
		} else if ("design".equals(action)) {
			design(req, resp);
		} else if ("ui".equals(action)) {
			ui(req, resp);
		}
		
		throw new UnsupportedOperationException("You must specify action");
	}

	private void core(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		resp.setContentType("application/xml");

		try {
			ExchangeManager.exportCoreSchema(resp.getOutputStream());
		} catch (Exception e) {
			throw new ServletException("error exporting", e);
		}
	}

	private void design(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		resp.setContentType("application/xml");

		try {
			ExchangeManager.exportDesignSchema(resp.getOutputStream());
		} catch (Exception e) {
			throw new ServletException("error exporting", e);
		}
	}

	private void ui(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		resp.setContentType("application/xml");

		try {
			ExchangeManager.exportUiSchema(resp.getOutputStream());
		} catch (Exception e) {
			throw new ServletException("error exporting", e);
		}
	}
}
