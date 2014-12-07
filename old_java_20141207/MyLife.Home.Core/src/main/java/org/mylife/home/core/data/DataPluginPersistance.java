package org.mylife.home.core.data;

/**
 * Donnée de persistance de plugin
 * 
 * @author pumbawoman
 * 
 */
public class DataPluginPersistance {

	private int id;
	private String componentId;
	private String key;
	private String value;

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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
