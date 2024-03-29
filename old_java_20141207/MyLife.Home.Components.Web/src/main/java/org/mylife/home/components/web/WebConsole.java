package org.mylife.home.components.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.mylife.home.common.web.model.ServerState;
import org.mylife.home.common.web.model.Severity;
import org.mylife.home.components.services.ManagerService;
import org.mylife.home.components.services.ServiceAccess;

/**
 * Servlet console
 * 
 * @author pumbawoman
 * 
 */
public class WebConsole extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 445840739835708708L;

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
		if ("serverState".equals(action)) {
			serverState(req, resp);
		} else if ("componentsState".equals(action)) {
			componentsState(req, resp);
		} else if ("structureState".equals(action)) {
			structureState(req, resp);
		} else if ("start".equals(action)) {
			start(req, resp);
		} else if ("stop".equals(action)) {
			stop(req, resp);
		} else {
			index(req, resp);
		}
	}

	private void serverState(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		ServerState serverState = new ServerState();
		int state = ServiceAccess.getInstance().getManagerService().getState();
		switch (state) {
		case ManagerService.STATE_STOPPED:
			serverState.setState("STOPPED");
			serverState.setSeverity(Severity.WARNING);
			serverState.setCanStop(false);
			serverState.setCanStart(true);
			break;

		case ManagerService.STATE_ERROR:
			serverState.setState("ERROR");
			serverState.setSeverity(Severity.ERROR);
			serverState.setError(ServiceAccess.getInstance()
					.getManagerService().getError());
			serverState.setCanStop(true);
			serverState.setCanStart(true);
			break;

		case ManagerService.STATE_RUNNING:
			serverState.setState("RUNNING");
			serverState.setSeverity(Severity.INFO);
			serverState.setCanStop(true);
			serverState.setCanStart(false);
			break;

		case ManagerService.STATE_STARTING:
			serverState.setState("STARTING");
			serverState.setSeverity(Severity.INFO);
			serverState.setCanStop(false);
			serverState.setCanStart(false);
			break;

		case ManagerService.STATE_STOPPING:
			serverState.setState("STOPPING");
			serverState.setSeverity(Severity.INFO);
			serverState.setCanStop(false);
			serverState.setCanStart(false);
			break;

		}
		req.setAttribute("data", serverState);

		req.getRequestDispatcher("/jsp/ServerState.jsp").forward(req, resp);
	}

	private void componentsState(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		req.setAttribute("data", ServiceAccess.getInstance()
				.getComponentService().getComponents());
		req.getRequestDispatcher("/jsp/ComponentsState.jsp").forward(req, resp);
	}

	private void structureState(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			WebStructure.exportStructure(this, stream);
		} catch (JAXBException e) {
			throw new ServletException("error exporting", e);
		}
		String data = stream.toString("utf-8");
		req.setAttribute("data", data);
		req.getRequestDispatcher("/jsp/StructureState.jsp").forward(req, resp);
	}

	private void start(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		ServiceAccess.getInstance().getManagerService().start();

		resp.sendRedirect(req.getRequestURI());
	}

	private void stop(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		ServiceAccess.getInstance().getManagerService().stop();

		resp.sendRedirect(req.getRequestURI());
	}

	private void index(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		req.setAttribute("title", "Console");
		req.getRequestDispatcher("/jsp/Console.jsp").forward(req, resp);
	}
}
