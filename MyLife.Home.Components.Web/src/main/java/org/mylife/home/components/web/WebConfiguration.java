package org.mylife.home.components.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mylife.home.components.providers.ComponentConfiguration;
import org.mylife.home.components.providers.ComponentFactory;
import org.mylife.home.components.services.ConfigurationService;
import org.mylife.home.components.services.ServiceAccess;

/**
 * Servlet de configuration
 * 
 * @author pumbawoman
 * 
 */
@MultipartConfig
public class WebConfiguration extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8986836924739824605L;

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
		if ("delete".equals(action)) {
			delete(req, resp);
		} else if ("create".equals(action)) {
			create(req, resp);
		} else if ("updateParameters".equals(action)) {
			updateParameters(req, resp);
		} else if ("updateForm".equals(action)) {
			updateForm(req, resp);
		} else if ("activate".equals(action)) {
			activate(req, resp);
		} else if ("deactivate".equals(action)) {
			deactivate(req, resp);
		} else {
			index(req, resp);
		}
	}

	private void delete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));
		ServiceAccess.getInstance().getConfigurationService().delete(id);

		resp.sendRedirect(req.getRequestURI());
	}

	private void create(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		ComponentConfiguration item = new ComponentConfiguration();
		item.setComponentId(req.getParameter("componentId"));
		item.setType(req.getParameter("type"));
		String[] names = req.getParameterValues("nameList");
		String[] values = req.getParameterValues("valueList");
		if (names != null && values != null) {
			for (int i = 0; i < names.length; i++) {
				item.getParameters().put(names[i], values[i]);
			}
		}

		ServiceAccess.getInstance().getConfigurationService().create(item);

		ServiceAccess.getInstance().getManagerService()
				.configurationUpdated(item);

		resp.sendRedirect(req.getRequestURI());
	}

	private void changeActivate(int id, boolean active) {

		ConfigurationService configurationService = ServiceAccess.getInstance()
				.getConfigurationService();
		ComponentConfiguration item = configurationService.get(id);
		if (item == null)
			return;

		if (item.isActive() == active)
			return;
		item.setActive(active);
		configurationService.update(item);
		ServiceAccess.getInstance().getManagerService()
				.configurationUpdated(item);
	}

	private void activate(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));
		changeActivate(id, true);

		resp.sendRedirect(req.getRequestURI());
	}

	private void deactivate(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));
		changeActivate(id, false);

		resp.sendRedirect(req.getRequestURI());
	}

	private void updateParameters(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {

		int id = Integer.parseInt(req.getParameter("id"));

		ConfigurationService configurationService = ServiceAccess.getInstance()
				.getConfigurationService();
		ComponentConfiguration item = configurationService.get(id);
		if (item != null) {

			item.getParameters().clear();
			String[] names = req.getParameterValues("nameList");
			String[] values = req.getParameterValues("valueList");
			for (int i = 0; i < names.length; i++) {
				item.getParameters().put(names[i], values[i]);
			}

			configurationService.update(item);

			ServiceAccess.getInstance().getManagerService()
					.configurationUpdated(item);
		}

		resp.sendRedirect(req.getRequestURI());
	}

	private void updateForm(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		ConfigurationService service = ServiceAccess.getInstance()
				.getConfigurationService();
		int id = Integer.parseInt(req.getParameter("id"));
		ComponentConfiguration data = service.get(id);
		if (data == null) {
			resp.sendRedirect(req.getRequestURI());
			return;
		}

		ComponentFactory factory = ServiceAccess.getInstance()
				.getComponentService().getFactory(data.getType());
		Collection<String> supportedParameters = factory.getParameterNames();
		if (supportedParameters == null)
			supportedParameters = Collections.emptyList();

		req.setAttribute("data", data);
		req.setAttribute("supportedParameters", supportedParameters);
		req.setAttribute("title", "Gestion de la configuration");
		req.getRequestDispatcher("/jsp/ConfigurationUpdateParameters.jsp")
				.forward(req, resp);
	}

	private void index(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// par défaut redirection vers la jsp
		ConfigurationService service = ServiceAccess.getInstance()
				.getConfigurationService();
		List<ComponentConfiguration> data = service.list();
		Map<String, String> types = service.listTypes();
		req.setAttribute("data", data);
		req.setAttribute("types", types);
		req.setAttribute("title", "Gestion de la configuration");
		req.getRequestDispatcher("/jsp/Configuration.jsp").forward(req, resp);
	}
}
