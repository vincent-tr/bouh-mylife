package org.mylife.home.core.web;

import java.text.SimpleDateFormat;
import java.util.Date;

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
}
