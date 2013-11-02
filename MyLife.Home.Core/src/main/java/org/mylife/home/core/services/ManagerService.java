package org.mylife.home.core.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service de gestion
 * @author pumbawoman
 *
 */
public class ManagerService {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(ManagerService.class
			.getName());

	public static final int STATE_STOPPED = 0;
	public static final int STATE_ERROR = -1;
	public static final int STATE_RUNNING = 1;

	public static final int STATE_STARTING = 2;
	public static final int STATE_STOPPING = 3;
	
	/* internal */ ManagerService() {
		
	}

	private int state;
	private Exception error;
	
	/**
	 * Gestion de l'exécution de start/stop
	 */
	private final ExecutorService commandExecution = Executors.newSingleThreadExecutor();
	
	/**
	 * Obtention de l'état
	 * @return
	 */
	public int getState() {
		return state;
	}
	
	private void setState(int state) {
		this.state = state;
	}
	
	/**
	 * Si l'état est à error, obtient l'erreur qui s'est produite, sinon null 
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
		
		switch(state) {
		case STATE_STARTING:
		case STATE_RUNNING:
		case STATE_STOPPING:
			log.warning("Invalid state, ignored");
			return;
		}
		
		setState(STATE_STARTING);
		
		try {
			executeStart();
		}
		catch(Exception e) {
			handleError(e);
			return;
		}
		
		setState(STATE_RUNNING);
	}
	
	private synchronized void stopImpl() {

		switch(state) {
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
		}
		catch(Exception e) {
			handleError(e);
			return;
		}
		
		setState(STATE_STOPPED);
	}
	
	private void executeStart() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
		
	}
	
	private void executeStop() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
		
	}
}
