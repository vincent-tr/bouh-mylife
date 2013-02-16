package mylife.home.hw.emulator.web;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mylife.home.hw.emulator.web.render.WebStream;
import aQute.bnd.annotation.component.Component;

@Component(provide=Servlet.class, properties={"alias="+DefaultServlet.path})
public class DefaultServlet extends HttpServlet {

	/**
	 * Chemin d'accès au servlet
	 */
	public static final String path = "/mylife.home.hw.emulator";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream());
		WebStream stream = new WebStream(writer);
		
		MainPage page = new MainPage();
		page.render(stream);
		writer.flush();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6430967627948050536L;

}
