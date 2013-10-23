package org.mylife.home.net.exchange;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "object")
public class XmlNetObject {

	public String id;
	@XmlElement(name = "class")
	public XmlNetClass clazz;
}
