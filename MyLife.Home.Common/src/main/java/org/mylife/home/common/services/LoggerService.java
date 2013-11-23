package org.mylife.home.common.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

/**
 * Service pour afficher les logs
 * 
 * @author pumbawoman
 * 
 */
public class LoggerService implements Service {

	private final int RECORD_MAX = 500;
	private final Logger usedLogger;
	private final LogHandler handler;
	private final CircularFifoBuffer buffer = new CircularFifoBuffer(RECORD_MAX);

	/* internal */LoggerService() {
		usedLogger = LogManager.getLogManager().getLogger("");
		handler = new LogHandler();
		usedLogger.addHandler(handler);
	}

	@Override
	public void terminate() {
		usedLogger.removeHandler(handler);
		buffer.clear();
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

	/**
	 * Ajout d'un enregistrement
	 * 
	 * @param record
	 */
	private synchronized void publishRecord(LogRecord record) {
		buffer.add(record);
	}

	/**
	 * Obtention d'une copie des enregistrements pour rester verrouiller le
	 * moins possible
	 * 
	 * @return
	 */
	private synchronized Object[] getRecords() {
		return buffer.toArray();
	}

	/**
	 * Obtention des logs
	 * 
	 * @param logger
	 * @param minLevel
	 * @param maxLevel
	 * @param maxCount
	 * @return
	 */
	public Collection<LogRecord> getLogs(String logger, int minLevel,
			int maxLevel, int maxCount) {

		if (maxCount == -1)
			maxCount = RECORD_MAX;

		Collection<LogRecord> ret = new ArrayList<LogRecord>();
		Object[] source = getRecords();

		for (Object o : source) {
			LogRecord record = (LogRecord) o;

			if (ret.size() >= maxCount)
				break;

			if (!record.getLoggerName().equals(logger))
				continue;

			int level = record.getLevel().intValue();
			if (level < minLevel || level > maxLevel)
				continue;

			ret.add(record);
		}

		return ret;
	}
}
