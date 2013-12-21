package org.mylife.home.core.exchange.ui;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "action")
@XmlSeeAlso({ XmlUiCoreAction.class, XmlUiPanelAction.class,
		XmlUiWindowAction.class })
public class XmlUiAction {
}
