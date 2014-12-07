package org.mylife.home.common.web.model;

/**
 * Etat du serveur
 * 
 * @author pumbawoman
 *
 */
public class ServerState {
	
	private String state;
	private Exception error;
	private boolean canStart;
	private boolean canStop;
	private int severity;
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Exception getError() {
		return error;
	}
	public void setError(Exception error) {
		this.error = error;
	}
	public boolean isCanStart() {
		return canStart;
	}
	public void setCanStart(boolean canStart) {
		this.canStart = canStart;
	}
	public boolean isCanStop() {
		return canStop;
	}
	public void setCanStop(boolean canStop) {
		this.canStop = canStop;
	}
	public int getSeverity() {
		return severity;
	}
	public void setSeverity(int severity) {
		this.severity = severity;
	}
	
}
