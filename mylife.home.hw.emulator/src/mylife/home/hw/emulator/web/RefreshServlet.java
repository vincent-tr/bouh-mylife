package mylife.home.hw.emulator.web;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import aQute.bnd.annotation.component.Component;

@Component(provide=Servlet.class, properties={"alias="+RefreshServlet.path})
public class RefreshServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4658064378080261268L;

	/**
	 * Chemin relatif
	 */
	private static final String relativePath = "/refresh";
	
	/**
	 * Chemin d'accès au servlet
	 */
	public static final String path = DefaultServlet.path + relativePath;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	}
}
