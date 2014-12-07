package org.mylife.home.net.hub.irc.tasks;

/**
 * Tâche avec attente possible de la fin d'exécution
 * 
 * @author pumbawoman
 * 
 */
public abstract class WaitableTask implements Runnable {

	private boolean terminated = false;
	private final Object terminatedEvent = new Object();

	@Override
	public final void run() {
		try {
			runTask();
		} finally {
			synchronized (terminatedEvent) {
				terminated = true;
				terminatedEvent.notifyAll();
			}
		}
	}

	public abstract void runTask();

	public void waitTask() throws InterruptedException {
		synchronized (terminatedEvent) {
			if (terminated)
				return;
			terminatedEvent.wait();
		}
	}
}
