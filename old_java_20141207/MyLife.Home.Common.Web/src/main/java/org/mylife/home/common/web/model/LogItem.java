package org.mylife.home.common.web.model;

import java.util.Date;

/**
 * Représentation d'un item de log
 * 
 * @author pumbawoman
 * 
 */
public class LogItem {

	private final Date date;
	private final String logger;
	private final String level;
	private final int severity;
	private final String message;
	private final String error;
	private final int threadId;

	public LogItem(Date date, String logger, String level, int severity,
			String message, String error, int threadId) {
		this.date = date;
		this.logger = logger;
		this.level = level;
		this.severity = severity;
		this.message = message;
		this.error = error;
		this.threadId = threadId;
	}

	public Date getDate() {
		return date;
	}

	public String getLogger() {
		return logger;
	}

	public String getLevel() {
		return level;
	}

	public int getSeverity() {
		return severity;
	}

	public String getMessage() {
		return message;
	}

	public String getError() {
		return error;
	}

	public int getThreadId() {
		return threadId;
	}
}
