package org.mylife.home.core.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Outils web
 * @author trumpffv
 *
 */
public class WebTools {

	/**
	 * HTML escape
	 * 
	 * @param input
	 * @return
	 */
	public static String htmlEscape(String input) {
		return StringEscapeUtils.escapeHtml4(input);
	}
	
	/**
	 * Formattage de date
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}
	
	/**
	 * Formattage de date
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {
		return formatDate(date, "yyyy-MM-dd HH:mm:ss");
	}
	
	public static String servlet(PageContext context, String name) {
		 String root = ((HttpServletRequest)context.getRequest()).getContextPath();
		 return root + "/" + name;
	}
	
	public static String image(PageContext context, String name) {
		 String root = ((HttpServletRequest)context.getRequest()).getContextPath();
		 return root + "/static/images/" + name;
	}
	
	public static String style(PageContext context, String name) {
		 String root = ((HttpServletRequest)context.getRequest()).getContextPath();
		 return root + "/static/style/" + name;
	}
}
