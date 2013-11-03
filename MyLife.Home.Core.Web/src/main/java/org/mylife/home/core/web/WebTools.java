package org.mylife.home.core.web;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mylife.home.core.web.model.Severity;

/**
 * Outils web
 * 
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
		return htmlEscape(input, true);
	}

	/**
	 * HTML escape
	 * 
	 * @param input
	 * @param escapeSpecials
	 * @return
	 */
	public static String htmlEscape(String input, boolean escapeSpecials) {
		String value = input;
		value = StringEscapeUtils.escapeHtml4(value);
		if (escapeSpecials) {
			value = value.replace("\n", "<br />\n");
			value = value.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		}
		return value;
	}

	/**
	 * Formattage de date
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}

	/**
	 * Formattage d'erreur
	 * 
	 * @param e
	 * @return
	 */
	public static String formatError(Exception e) {
		if (e == null)
			return "";

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ExceptionUtils.printRootCauseStackTrace(e, new PrintStream(baos));
		return htmlEscape(baos.toString());
	}

	/**
	 * Obtient le nom de l'image correspondant à la gravité donnée
	 * 
	 * @param context
	 * @param severity
	 * @return
	 */
	public static String severityImage(PageContext context, int severity) {
		switch (severity) {
		case Severity.HELP:
			return image(context, "help.png");
		case Severity.INFO:
			return image(context, "info.png");
		case Severity.WARNING:
			return image(context, "warning.png");
		case Severity.ERROR:
			return image(context, "error.png");
		default:
			return null;
		}
	}

	/**
	 * Obtient la couleur correspondant à la gravité donnée
	 * 
	 * @param severity
	 * @return
	 */
	public static String severityColor(int severity) {
		switch (severity) {
		case Severity.HELP:
			return "#0000FF";
		case Severity.INFO:
			return "#00CC00";
		case Severity.WARNING:
			return "#FF6600";
		case Severity.ERROR:
			return "#FF0000";
		default:
			return null;
		}
	}

	/**
	 * Formattage de date
	 * 
	 * @param date
	 * @return
	 */
	public static String formatDate(Date date) {
		return formatDate(date, "yyyy-MM-dd HH:mm:ss");
	}

	public static String servlet(PageContext context, String name) {
		String root = ((HttpServletRequest) context.getRequest())
				.getContextPath();
		return root + "/" + name;
	}

	public static String image(PageContext context, String name) {
		String root = ((HttpServletRequest) context.getRequest())
				.getContextPath();
		return root + "/static/images/" + name;
	}

	public static String style(PageContext context, String name) {
		String root = ((HttpServletRequest) context.getRequest())
				.getContextPath();
		return root + "/static/styles/" + name;
	}

	public static String script(PageContext context, String name) {
		String root = ((HttpServletRequest) context.getRequest())
				.getContextPath();
		return root + "/static/scripts/" + name;
	}
}
