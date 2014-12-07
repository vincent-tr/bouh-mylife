package org.mylife.home.ui.web;

import javax.servlet.http.HttpServletRequest;

import org.mylife.home.ui.services.ServiceAccess;

public class WebStructure extends WebJsonBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3116609282185443756L;

	@Override
	protected Object getObjectData(HttpServletRequest req) {
		return ServiceAccess.getInstance().getNetService().getUiContainer();
	}

}
