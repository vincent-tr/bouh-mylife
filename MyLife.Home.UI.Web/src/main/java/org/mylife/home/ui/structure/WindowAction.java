package org.mylife.home.ui.structure;

public class WindowAction extends Action {
	private final String windowId;
	private final boolean popup;

	public WindowAction(String windowId, boolean popup) {
		this.windowId = windowId;
		this.popup = popup;
	}

	public String getWindowId() {
		return windowId;
	}

	public boolean isPopup() {
		return popup;
	}

}
