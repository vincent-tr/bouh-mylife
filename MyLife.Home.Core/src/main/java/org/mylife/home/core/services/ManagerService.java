package org.mylife.home.core.services;

/**
 * Service de gestion
 * @author pumbawoman
 *
 */
public class ManagerService {

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
	 * Obtention de l'état
	 * @return
	 */
	public int getState() {
		return state;
	}
	
	/**
	 * Si l'état est à error, renvoit l'erreur qui s'est produite, sinon null 
	 * @return
	 */
	public Exception getError() {
		return error;
	}
	
	/**
	 * Démarrage
	 */
	public synchronized void start() {
		state = STATE_RUNNING;
	}
	
	/**
	 * Arrêt
	 */
	public synchronized void stop() {
		state = STATE_STOPPED;
	}
}
