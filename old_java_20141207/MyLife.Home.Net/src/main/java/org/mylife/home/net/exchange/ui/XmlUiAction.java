package org.mylife.home.net.exchange.ui;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "action")
@XmlSeeAlso({ XmlUiCoreAction.class, XmlUiWindowAction.class })
public class XmlUiAction {
}
