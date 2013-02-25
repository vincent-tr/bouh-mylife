package mylife.home.hw.emulator.web;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mylife.home.hw.emulator.device.DeviceImpl;
import mylife.home.hw.emulator.device.Manager;
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
		
		OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream());

		writer.write("<emulator>\n");
		writer.write("\t<pins>");
		
		for(Integer id : Manager.getInstance().getValidPins()) {
			DeviceImpl device = Manager.getInstance().getOpenedDevice(id);
			writer.write("\t\t<pin id=\"\" type=\"\" />");
		}
		
		writer.write("\t</pins>");
		writer.write("</emulator>\n");
		
		writer.flush();
	}
}
