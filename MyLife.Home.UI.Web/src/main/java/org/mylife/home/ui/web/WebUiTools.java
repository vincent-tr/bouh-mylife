package org.mylife.home.ui.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.mylife.home.common.web.WebTools;

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
		return WebTools.servlet(context, "");
	}
}
