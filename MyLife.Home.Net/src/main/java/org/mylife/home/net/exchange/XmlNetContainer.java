package org.mylife.home.net.exchange;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "netContainer")
public class XmlNetContainer {

	public XmlNetObject[] components;
	public String componentsVersion;
	public String documentVersion;
}
