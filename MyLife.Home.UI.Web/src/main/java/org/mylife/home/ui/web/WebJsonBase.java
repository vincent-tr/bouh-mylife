package org.mylife.home.ui.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

/**
 * Servlet de base pour retour json
 * @author pumbawoman
 *
 */
public abstract class WebJsonBase extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7766028135173894545L;

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
		
		Object data = getObjectData(req);
		
		
		resp.setContentType("application/json");
		PrintWriter writer = resp.getWriter();

		Gson gson = new Gson();
		gson.toJson(data, writer);
		writer.flush();
	}
	
	protected abstract Object getObjectData(HttpServletRequest req);
}
