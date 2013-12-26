package org.mylife.home.ui.structure;

public class CoreAction extends Action {

	private final String componentId;
	private final String componentAction;

	public CoreAction(String componentId, String componentAction) {
		this.componentId = componentId;
		this.componentAction = componentAction;
	}

	public String getComponentId() {
		return componentId;
	}

	public String getComponentAction() {
		return componentAction;
	}
}
