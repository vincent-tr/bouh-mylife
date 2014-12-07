package org.mylife.home.raspberry.gpio;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Monitoring des entrées changées
 * 
 * @author pumbawoman
 * 
 */
class GpioInputMonitor implements Runnable {

	public final static int INTERVAL = 100;

	private class Checker {

		private final GpioAccess access;
		private long lastModified;
		private File file;

		public Checker(GpioAccess access) {
			this.access = access;

			String path = access.getBasePath() + "value";
			file = new File(path);
			lastModified = file.lastModified();
		}

		public void check() {
			long newModified = file.lastModified();
			if (lastModified == newModified)
				return;

			newModified = lastModified;
			access.fireListeners();
		}
	}

	private static final GpioInputMonitor instance = new GpioInputMonitor();

	public static final GpioInputMonitor getInstance() {
		return instance;
	}

	private GpioInputMonitor() {
	}

	private Map<GpioAccess, Checker> monitoredPins = new HashMap<GpioAccess, Checker>();
	private Thread worker;

	/**
	 * Démarrage du monitoring
	 * 
	 * @param access
	 */
	public synchronized void startMonitoring(GpioAccess access) {
		monitoredPins.put(access, new Checker(access));
		if (worker == null) {
			worker = new Thread(this);
			worker.start();
		}
	}

	/**
	 * Arrêt du monitoring
	 * 
	 * @param access
	 */
	public synchronized void stopMonitoring(GpioAccess access) {
		monitoredPins.remove(access);
	}

	/**
	 * Worker
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				// interrompu
			}

			if (!check()) {
				// si plus d'élément fin du thread
				worker = null;
				return;
			}
		}
	}

	private synchronized boolean check() {
		// si plus d'élément fin du thread
		if(monitoredPins.size() == 0)
			return false;
		
		for(Checker checker : monitoredPins.values())
			checker.check();
		
		return true;
	}
}
