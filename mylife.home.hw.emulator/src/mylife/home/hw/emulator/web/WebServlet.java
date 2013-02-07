package mylife.home.hw.emulator.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import aQute.bnd.annotation.component.Component;

@Component(provide=Servlet.class, properties={"alias=/mylife.home.hw.emulator"})
public class WebServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		//resp.getWriter().write("Hello World");
		OutputStream outputStream = resp.getOutputStream();
		InputStream inputStream = WebServlet.class.getResourceAsStream("/mylife/home/hw/emulator/web/Layout.png");
		
		byte[] buf = new byte[8192];
		while (true) {
			int length = inputStream.read(buf);
			if (length < 0)
				break;
			outputStream.write(buf, 0, length);
		}
		outputStream.flush();

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6430967627948050536L;

}
