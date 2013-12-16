package org.mylife.home.components.providers;

import java.util.HashMap;
import java.util.Map;

public class ComponentConfiguration {

	private int dataId;
	private String componentId;
	private String type;
	private boolean active;
	private final Map<String, String> parameters = new HashMap<String, String>();

	public int getDataId() {
		return dataId;
	}

	public void setDataId(int dataId) {
		this.dataId = dataId;
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

	public Map<String, String> getParameters() {
		return parameters;
	}
}
