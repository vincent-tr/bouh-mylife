package org.mylife.home.core.exchange.design;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class XmlDesignConfiguration {

	public String name;
	public String displayName;
	public XmlDesignValueType type;
	public boolean mandatory;
	
	@XmlElementWrapper(name = "possibleValues")
	@XmlElement(name = "value")
	public String[] possibleValues;
}
