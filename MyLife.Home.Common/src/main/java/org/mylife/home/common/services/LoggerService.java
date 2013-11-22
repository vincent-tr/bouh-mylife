package org.mylife.home.common.services;

import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerService implements Service {

	private final int RECORD_MAX = 500;
	private final Logger usedLogger;
	private final LogHandler handler;
	
	/* internal */LoggerService() {
		usedLogger = LogManager.getLogManager().getLogger("");
		handler = new LogHandler();
		usedLogger.addHandler(handler);
	}
	
	@Override
	public void terminate() {
		usedLogger.removeHandler(handler);
	}
	
	private class LogHandler extends Handler {

		@Override
		public void close() throws SecurityException {
		}

		@Override
		public void flush() {
		}

		@Override
		public void publish(LogRecord record) {
			publishRecord(record);
		}
	}

	private void publishRecord(LogRecord record) {
		// TODO
	}
}
