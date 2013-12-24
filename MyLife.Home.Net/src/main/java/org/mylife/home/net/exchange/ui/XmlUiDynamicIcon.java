package org.mylife.home.net.exchange.ui;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "dynamicIcon")
@XmlSeeAlso({ XmlUiRangeIcon.class, XmlUiEnumIcon.class})
public class XmlUiDynamicIcon {

	public String defaultIconId;
}
