package org.mylife.home.net.exchange.ui;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "dynamicIcon")
@XmlSeeAlso({ XmlUiRangeIcon.class, XmlUiEnumIcon.class})
public class XmlUiDynamicIcon extends XmlUiIcon {

	public String componentId;
	public String componentAttribute;
	public String defaultIconId;
}
