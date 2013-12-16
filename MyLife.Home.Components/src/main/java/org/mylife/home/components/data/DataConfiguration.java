package org.mylife.home.components.data;

/**
 * Configuration
 * 
 * @author pumbawoman
 * 
 */
public class DataConfiguration {
	private int id;
	private String componentId;
	private String type;
	private boolean active;
	private byte[] parameters;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public byte[] getParameters() {
		return parameters;
	}

	public void setParameters(byte[] parameters) {
		this.parameters = parameters;
	}
}
