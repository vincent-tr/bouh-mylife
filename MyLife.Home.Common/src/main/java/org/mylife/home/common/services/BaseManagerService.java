package org.mylife.home.common.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service de gestion
 * 
 * @author pumbawoman
 * 
 */
public abstract class BaseManagerService implements Service {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(BaseManagerService.class
			.getName());

	public static final int STATE_STOPPED = 0;
	public static final int STATE_ERROR = -1;
	public static final int STATE_RUNNING = 1;

	public static final int STATE_STARTING = 2;
	public static final int STATE_STOPPING = 3;

	protected BaseManagerService() {
	}

	@Override
	public void terminate() {
		stop();
		commandExecution.shutdown();
		try {
			commandExecution.awaitTermination(Long.MAX_VALUE,
					TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			log.log(Level.SEVERE,
					"commandExecution.awaitTermination interrupted on terminate",
					e);
		}
	}

	private int state;
	private Exception error;

	/**
	 * Gestion de l'exécution de start/stop
	 */
	private final ExecutorService commandExecution = Executors
			.newSingleThreadExecutor();

	/**
	 * Obtention de l'état
	 * 
	 * @return
	 */
	public int getState() {
		return state;
	}

	private void setState(int state) {
		this.state = state;
		log.info("Setting level to : " + getStateString());
	}

	/**
	 * Obtention de l'état en chaine
	 * 
	 * @return
	 */
	public String getStateString() {
		switch (state) {
		case STATE_STOPPED:
			return "STOPPED";
		case STATE_ERROR:
			return "ERROR";
		case STATE_RUNNING:
			return "RUNNING";
		case STATE_STARTING:
			return "STARTING";
		case STATE_STOPPING:
			return "STOPPING";
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Si l'état est à error, obtient l'erreur qui s'est produite, sinon null
	 * 
	 * @return
	 */
	public Exception getError() {
		return error;
	}

	private void handleError(Exception e) {
		setState(STATE_ERROR);
		error = e;
		log.log(Level.SEVERE, "Severe error in manager service", e);
	}

	/**
	 * Démarrage
	 */
	public void start() {
		commandExecution.submit(new Start());
	}

	/**
	 * Arrêt
	 */
	public void stop() {
		commandExecution.submit(new Stop());
	}

	private class Start implements Runnable {
		@Override
		public void run() {
			startImpl();
		}
	}

	private class Stop implements Runnable {
		@Override
		public void run() {
			stopImpl();
		}
	}

	private synchronized void startImpl() {

		switch (state) {
		case STATE_STARTING:
		case STATE_RUNNING:
		case STATE_STOPPING:
			log.warning("Invalid state, ignored");
			return;
		}

		setState(STATE_STARTING);

		try {
			executeStart();
		} catch (Exception e) {
			handleError(e);
			return;
		}

		setState(STATE_RUNNING);
	}

	private synchronized void stopImpl() {

		switch (state) {
		case STATE_STARTING:
		case STATE_STOPPED:
		case STATE_STOPPING:
			log.warning("Invalid state, ignored");
			return;

		case STATE_ERROR:
			// seulement clear pour passer de error à stopped
			error = null;
			setState(STATE_STOPPED);
			return;
		}

		setState(STATE_STOPPING);

		try {
			executeStop();
		} catch (Exception e) {
			handleError(e);
			return;
		}

		setState(STATE_STOPPED);
	}

	/**
	 * Exécution du démarrage
	 */
	protected abstract void executeStart() throws Exception;
	
	/**
	 * Exécution de l'arrêt
	 */
	protected abstract void executeStop() throws Exception;
}
