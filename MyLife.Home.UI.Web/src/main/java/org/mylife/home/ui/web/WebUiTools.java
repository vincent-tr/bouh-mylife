package org.mylife.home.ui.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

public class WebUiTools {


	public static String image(PageContext context, String id) {
		String root = ((HttpServletRequest) context.getRequest())
				.getContextPath();
		return root + "/image?id=" + id;
	}
}
