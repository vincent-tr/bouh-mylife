package mylife.home.hw.emulator.web;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mylife.home.hw.emulator.device.DeviceImpl;
import mylife.home.hw.emulator.device.InputDeviceImpl;
import mylife.home.hw.emulator.device.Manager;

import aQute.bnd.annotation.component.Component;

@Component(provide=Servlet.class, properties={"alias="+DataServlet.path})
public class DataServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6082437773428093508L;

	/**
	 * Chemin relatif
	 */
	private static final String relativePath = "/data";
	
	/**
	 * Chemin d'accès au servlet
	 */
	public static final String path = DefaultServlet.path + relativePath;


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		executeRequest(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		executeRequest(req, resp);
	}
	
	private void executeRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		readData(req);
		writeData(resp);
	}
	
	private void readData(HttpServletRequest req) {
		
		// obtention du pin et de sa valeur s'il y en a une
		String sPinId = req.getParameter("pinId"); 
		String sValue = req.getParameter("value");
		
		if(sPinId == null || "".equals(sPinId) ||
				sValue == null || "".equals(sValue)) {
			return;
		}
		
		int pinId = Integer.parseInt(sPinId);
		boolean value = Boolean.parseBoolean(sValue);
		
		// définition de la valeur du pin s'il est ouvert et en entrée
		DeviceImpl device = Manager.getInstance().getOpenedDevice(pinId);
		if(device != null && device instanceof InputDeviceImpl)
			((InputDeviceImpl)device).setValue(value);
	}
	
	private void writeData(HttpServletResponse resp) throws IOException {
		
		resp.setContentType("text/xml");
		OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream());

		writer.write("<emulator>\n");
		writer.write("\t<pins>");
		
		for(DeviceImpl device : Manager.getInstance().getOpenedDevices()) {
			
			int id = device.getPinId();
			String type = device.getType();
			String status = device.getStatus();
			writer.write("\t\t<pin id=\"" + id + "\" type=\"" + type + "\" status=\"" + status + "\" />\n");
		}
		
		writer.write("\t</pins>");
		writer.write("</emulator>\n");
		
		writer.flush();
	}
}
