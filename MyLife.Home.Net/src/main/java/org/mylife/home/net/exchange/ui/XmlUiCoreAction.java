package org.mylife.home.net.exchange.ui;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "coreAction")
public class XmlUiCoreAction extends XmlUiAction {
	
	public String componentId;
	public String componentMethod;
	
}
