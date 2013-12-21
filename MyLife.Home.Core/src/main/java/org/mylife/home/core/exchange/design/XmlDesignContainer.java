package org.mylife.home.core.exchange.design;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "designContainer")
public class XmlDesignContainer {

	@XmlElementWrapper(name = "plugins")
	@XmlElement(name = "plugin")
	public XmlDesignPlugin[] plugins;

	public String documentName;
	public String documentVersion;
}
