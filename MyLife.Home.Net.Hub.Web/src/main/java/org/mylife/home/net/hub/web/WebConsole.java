package org.mylife.home.net.hub.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.mylife.home.common.web.model.Severity;
import org.mylife.home.net.hub.irc.IrcConfiguration;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.services.LinkService;
import org.mylife.home.net.hub.services.LinkService.RunningLink;
import org.mylife.home.net.hub.services.ManagerService;
import org.mylife.home.net.hub.services.ServiceAccess;
import org.mylife.home.net.hub.web.model.IrcServerState;
import org.mylife.home.net.hub.web.model.NetworkView;

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
		} else if ("networkState".equals(action)) {
			networkState(req, resp);
		} else if ("linksState".equals(action)) {
			linksState(req, resp);
		} else if ("linksClose".equals(action)) {
			linksClose(req, resp);
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

		IrcServerState serverState = new IrcServerState();
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
			serverState.setError(ServiceAccess.getInstance().getManagerService().getError());
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
		
		IrcServer server = ServiceAccess.getInstance().getManagerService().getServer();
		serverState.setIrcServer(server != null);
		if (server != null) {
			IrcConfiguration config = server.getConfig();
			serverState.setIrcServerName(server.getServerName());
			serverState.setIrcNetworkName(server.getNetworkName());
			
			serverState.setIrcListeners(new ArrayList<String>());
			for (IrcConfiguration.Listener listener : config.getListeners()) {
				String address = listener.getAddress();
				if(StringUtils.isEmpty(address))
					address = "localhost";
				
				serverState.getIrcListeners().add(address + ":" + listener.getPort());
			}			
		}
		
		req.setAttribute("data", serverState);
		req.getRequestDispatcher("/jsp/ServerState.jsp").forward(req, resp);
	}
	
	private void networkState(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		IrcServer server = ServiceAccess.getInstance().getManagerService().getServer();
		if(server != null)
			req.setAttribute("data", NetworkView.getView(server));
		req.getRequestDispatcher("/jsp/NetworkState.jsp").forward(req, resp);
	}
	
	private void linksClose(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		// On cherche un lien à fermer
		String serverName = req.getParameter("server");
		LinkService service = ServiceAccess.getInstance().getLinkService();
		Set<RunningLink> links = service.getRunning();
		for(RunningLink link : links) {
			if(link.getServerName().equals(serverName)) {
				service.closeLink(link);
				break;
			}
		}
		
		resp.sendRedirect(req.getRequestURI());
	}
	
	private void linksState(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		Set<RunningLink> links = ServiceAccess.getInstance().getLinkService().getRunning();
		req.setAttribute("data", links);
		req.getRequestDispatcher("/jsp/LinksState.jsp").forward(req, resp);
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
