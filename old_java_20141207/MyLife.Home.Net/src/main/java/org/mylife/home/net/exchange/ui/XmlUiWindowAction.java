package org.mylife.home.net.exchange.ui;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "windowAction")
public class XmlUiWindowAction extends XmlUiAction {

	public String windowId;
	public boolean popup;
}
