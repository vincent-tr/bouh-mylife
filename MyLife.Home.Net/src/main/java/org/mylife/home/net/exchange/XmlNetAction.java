package org.mylife.home.net.exchange;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "action")
public class XmlNetAction extends XmlNetMember {

	@XmlElementWrapper(name = "arguments")
	@XmlElement(name = "argument")
	public XmlNetType[] arguments;
}
