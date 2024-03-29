package org.mylife.home.net.exchange.ui;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "rangeIcon")
public class XmlUiRangeIcon extends XmlUiDynamicIcon {

	@XmlElementWrapper(name = "mappings")
	@XmlElement(name = "mapping")
	public XmlUiRangeIconMapping[] mappings;
}
