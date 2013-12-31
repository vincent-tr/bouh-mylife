package org.mylife.home.ui.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

public class WebUiTools {

	public static String image(PageContext context, String id) {
		String root = ((HttpServletRequest) context.getRequest())
				.getContextPath();
		return root + "/image?id=" + id;
	}

	public static String webSocketUrl(PageContext context) {
		HttpServletRequest req = (HttpServletRequest) context.getRequest();

		String serverName = req.getServerName();
		int serverPort = req.getServerPort();
		String contextPath = req.getContextPath();
		StringBuffer builder = new StringBuffer();

		builder.append("ws://");
		builder.append(serverName);
		builder.append(":");
		builder.append(serverPort);
		builder.append(contextPath);
		builder.append("/net");
		return builder.toString();
	}
	
	public static String root(PageContext context) {
		String root = ((HttpServletRequest) context.getRequest())
				.getContextPath();
		return root;
	}
	
	public static String partial(PageContext context) {
		String root = ((HttpServletRequest) context.getRequest())
				.getContextPath();
		return root + "/static/partials/";
	}
}
