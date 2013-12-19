package org.mylife.home.net.exchange;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "enum")
public class XmlNetEnum extends XmlNetType {

	@XmlElementWrapper(name = "values")
	@XmlElement(name = "value")
	public String[] values;
}
