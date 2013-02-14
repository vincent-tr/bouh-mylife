package mylife.home.hw.emulator.web;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mylife.home.hw.emulator.device.DeviceImpl;
import mylife.home.hw.emulator.device.InputDeviceImpl;
import mylife.home.hw.emulator.device.Manager;
import aQute.bnd.annotation.component.Component;

@Component(provide=Servlet.class, properties={"alias="+UpdateServlet.path})
public class UpdateServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1275880038126987417L;

	/**
	 * Chemin relatif
	 */
	private static final String relativePath = "/update";
	
	/**
	 * Chemin d'accès au servlet
	 */
	public static final String path = DefaultServlet.path + relativePath;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		// obtention du pin et de sa valeur
		int pinId = Integer.parseInt(req.getParameter("pinId"));
		boolean value = Boolean.parseBoolean(req.getParameter("value"));
		
		// définition de la valeur du pin s'il est ouvert et en entrée
		DeviceImpl device = Manager.getInstance().getOpenedDevice(pinId);
		if(device != null && device instanceof InputDeviceImpl)
			((InputDeviceImpl)device).setValue(value);
		
		// TODO : renvoi réponse statut
	}
}
