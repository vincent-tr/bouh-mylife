package org.mylife.home.net.hub.irc;

import org.mylife.home.net.hub.irc.io.IOManager;

public class IrcServer extends Thread {

	public static final int STATE_STOPPED = 0;
	public static final int STATE_ERROR = -1;
	public static final int STATE_RUNNING = 1;
	public static final int STATE_STARTING = 2;
	public static final int STATE_STOPPING = 3;

	private static final int SELECT_TIMEOUT = 10000;

	private boolean exit;
	private final IrcConfiguration config;
	private int status = STATE_STOPPED;
	private Exception fatalError;

	private IOManager iom;

	public IrcConfiguration getConfig() {
		return config;
	}

	public int getStatus() {
		return status;
	}

	public Exception getFatalError() {
		return fatalError;
	}

	public IrcServer(IrcConfiguration config) {
		this.config = config;
	}

	public void close() {
		exit = true;
		iom.wakeup();
	}

	@Override
	public void run() {

		try {
			status = STATE_STARTING;
			fatalError = null;

			init();
			status = STATE_RUNNING;
			try {
				execute();
			} finally {
				status = STATE_STOPPING;
				terminate();
			}
			status = STATE_STOPPED;

		} catch (Exception ex) {
			fatalError = ex;
			status = STATE_ERROR;
		}
	}

	private void execute() throws Exception {
		exit = false;
		while(!exit) {
			select();
			scheduleTasks();
		}
	}

	private void init() throws Exception {
		iom = new IOManager();
		// TODO
	}

	private void terminate() throws Exception {
		// TODO
		iom.close();
		iom = null;
	}
	
	private void select() throws Exception {
		iom.select(SELECT_TIMEOUT);
	}
	
	private void scheduleTasks() throws Exception {
		// TODO
	}
}
