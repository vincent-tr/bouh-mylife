package org.mylife.home.net.exchange.design;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.mylife.home.net.exchange.net.XmlNetType;

public class XmlDesignAction {

	public String name;
	public String displayName;

	@XmlElementWrapper(name = "arguments")
	@XmlElement(name = "argument")
	public XmlNetType[] arguments;

}
