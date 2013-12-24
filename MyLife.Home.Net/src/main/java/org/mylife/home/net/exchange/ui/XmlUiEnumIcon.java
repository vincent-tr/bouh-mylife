package org.mylife.home.net.exchange.ui;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "enumIcon")
public class XmlUiEnumIcon {

	@XmlElementWrapper(name = "mappings")
	@XmlElement(name = "mapping")
	public XmlUiEnumIconMapping[] mappings;
}
