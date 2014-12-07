package org.mylife.home.ui.web;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.mylife.home.ui.services.ServiceAccess;

/**
 * Démarrage automatique du gestionnaire du core
 * @author pumbawoman
 *
 */
public class AutoStartStopManagerService extends GenericServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1759776710452226563L;

	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		throw new ServletException("no service");
	}

	@Override
	public void init() throws ServletException {
		ServiceAccess.getInstance().getNetService().start();
	}

	@Override
	public void destroy() {
		ServiceAccess.getInstance().terminate();
	}
}
