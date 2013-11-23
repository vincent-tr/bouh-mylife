package org.mylife.home.common.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mylife.home.common.services.BaseServiceAccess;
import org.mylife.home.common.services.LoggerService;
import org.mylife.home.common.web.model.LogItem;
import org.mylife.home.common.web.model.Severity;

public class WebLogging extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7590841058508263465L;

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

		index(req, resp);
	}

	private void index(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// par défaut redirection vers la jsp
		LoggerService service = BaseServiceAccess.getBaseInstance()
				.getLoggerService();
		
		String logger = getDefaultableParameter(req, "logger", "");
		int minLevel = getDefaultableParameter(req, "minLevel", -1);
		int maxLevel = getDefaultableParameter(req, "maxLevel", -1);
		int maxCount = getDefaultableParameter(req, "maxCount", -1);
		boolean showError = getDefaultableParameter(req, "showError", false);
		
		Collection<LogRecord> records = service.getLogs(logger, minLevel, maxLevel, maxCount);
		List<LogItem> data = mapLogs(records, showError);
		List<Integer> maxCountValues = service.getMaxCountValues();
		Map<Integer, String> levels = service.getLevelValues();
		//Set<String> loggers = service.getLoggersList();

		req.setAttribute("logger", logger);
		req.setAttribute("minLevel", minLevel);
		req.setAttribute("maxLevel", maxLevel);
		req.setAttribute("maxCount", maxCount);
		
		req.setAttribute("data", data);
		req.setAttribute("maxCountValues", maxCountValues);
		//req.setAttribute("loggers", loggers);
		req.setAttribute("levels", levels);
		req.setAttribute("showError", showError);
		req.setAttribute("title", "Affichage des logs");
		req.getRequestDispatcher("/jsp/Logging.jsp").forward(req, resp);
	}

	private int mapSeverity(LogRecord record) {
		int value = record.getLevel().intValue();
		if (value < Level.WARNING.intValue()) {
			return Severity.INFO;
		} else if (value < Level.SEVERE.intValue()) {
			return Severity.WARNING;
		} else
			return Severity.ERROR;
	}

	private String mapError(LogRecord record, boolean mapError) {
		if (!mapError)
			return null;

		Throwable thrown = record.getThrown();
		if (thrown == null)
			return null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ExceptionUtils.printRootCauseStackTrace(thrown, new PrintStream(baos));
		return baos.toString();
	}

	private LogItem mapLog(LogRecord record, boolean mapError) {
		int severity = mapSeverity(record);
		String error = mapError(record, mapError);
		return new LogItem(new Date(record.getMillis()),
				record.getLoggerName(), record.getLevel().getName(), severity,
				record.getMessage(), error, record.getThreadID());
	}

	private List<LogItem> mapLogs(Collection<LogRecord> records,
			boolean mapError) {
		List<LogItem> list = new ArrayList<LogItem>();
		for (LogRecord record : records) {
			LogItem item = mapLog(record, mapError);
			list.add(item);
		}
		return list;
	}
	
	private String getDefaultableParameter(HttpServletRequest req, String name, String defaultValue) {
		if(!req.getParameterMap().containsKey(name))
			return defaultValue;
		return req.getParameter(name);
	}
	
	private int getDefaultableParameter(HttpServletRequest req, String name, int defaultValue) {
		if(!req.getParameterMap().containsKey(name))
			return defaultValue;
		return Integer.parseInt(req.getParameter(name));
	}
	
	private boolean getDefaultableParameter(HttpServletRequest req, String name, boolean defaultValue) {
		if(!req.getParameterMap().containsKey(name))
			return defaultValue;
		return Boolean.parseBoolean(req.getParameter(name));
	}
}
