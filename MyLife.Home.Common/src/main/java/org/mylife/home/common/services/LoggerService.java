package org.mylife.home.common.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Handler;
import java.util.logging.Level;
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
		if (minLevel == -1)
			minLevel = Integer.MIN_VALUE;
		if (maxLevel == -1)
			maxLevel = Integer.MAX_VALUE;

		Collection<LogRecord> ret = new ArrayList<LogRecord>();
		Object[] source = getRecords();

		int start = source.length - maxCount;
		if(start < 0)
			start = 0;
		for(int i=start; i<source.length; i++) {
			LogRecord record = (LogRecord) source[i];

			if (!record.getLoggerName().startsWith(logger))
				continue;

			int level = record.getLevel().intValue();
			if (level < minLevel || level > maxLevel)
				continue;

			ret.add(record);
		}

		return ret;
	}

	/**
	 * Obtention des loggers triés en liste
	 * 
	 * @return
	 *//*
	public Set<String> getLoggersList() {

		TreeNode<String> source = getLoggersTree();
		Set<String> loggers = new HashSet<String>();
		fillLogger(source, loggers);
		return loggers;
	}
	
	private void fillLogger(TreeNode<String> source, Set<String> loggers) {
		loggers.add(getLoggerFullName(source));
		for(TreeNode<String> child : source.getChildren())
			fillLogger(child, loggers);
	}*/

	/**
	 * Obtention des loggers triés en arbre
	 * 
	 * @return
	 *//*
	public TreeNode<String> getLoggersTree() {

		Object[] source = getRecords();

		// Loggers triés par taille de chaine
		SortedSet<String> loggersNoHierarchy = new TreeSet<String>(
				new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						int ret = ((Integer) o1.length()).compareTo(o2.length());
						if (ret != 0)
							return ret;
						return o1.compareTo(o2);
					}
				});
		for (Object o : source) {
			LogRecord record = (LogRecord) o;
			loggersNoHierarchy.add(record.getLoggerName());
		}

		// Construction de l'arbre
		TreeNode<String> root = new TreeNode<String>();
		root.setElement("");
		for (String logger : loggersNoHierarchy) {

			TreeNode<String> parent = findParentLogger(root, logger);
			String parentName = getLoggerFullName(parent);
			TreeNode<String> current = new TreeNode<String>();
			
			int parentLength = 0;
			if(parentName.length() > 0) {
				// +1 pour le .
				parentLength = parentName.length() + 1;
			}
				
			current.setElement(logger.substring(parentLength));
			current.setParent(parent);
			parent.getChildren().add(current);
		}

		return root;

	}*/

	/**
	 * Trouve le logger parent pour le nom de logger spécifié
	 * 
	 * @param current
	 * @param logger
	 * @return
	 *//*
	private TreeNode<String> findParentLogger(TreeNode<String> current,
			String logger) {
		for (TreeNode<String> child : current.getChildren()) {
			String name = getLoggerFullName(child);
			if (logger.startsWith(name))
				return findParentLogger(child, logger);
		}

		return current;
	}*/

	/**
	 * Obtention le nom complet du logger spécifié
	 * 
	 * @param node
	 * @return
	 *//*
	public String getLoggerFullName(TreeNode<String> node) {
		TreeNode<String> parent = node.getParent();
		if (parent == null)
			return node.getElement();
		else
			return getLoggerFullName(parent) + "." + node.getElement();
	}*/

	/**
	 * Liste des valeurs pour max count
	 * 
	 * @return
	 */
	public List<Integer> getMaxCountValues() {
		List<Integer> values = new ArrayList<Integer>();
		final int RECORD_STEP = 50;
		for (int i = RECORD_STEP; i <= RECORD_MAX; i += RECORD_STEP)
			values.add(i);
		return values;
	}

	public SortedMap<Integer, String> getLevelValues() {
		SortedMap<Integer, String> values = new TreeMap<Integer, String>();
		values.put(-1, "(Aucun)");
		addLevel(Level.CONFIG, values);
		addLevel(Level.FINE, values);
		addLevel(Level.FINER, values);
		addLevel(Level.FINEST, values);
		addLevel(Level.INFO, values);
		addLevel(Level.SEVERE, values);
		addLevel(Level.WARNING, values);
		return values;
	}

	private void addLevel(Level level, SortedMap<Integer, String> values) {
		values.put(level.intValue(), level.getName());
	}
}
