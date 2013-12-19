package org.mylife.home.net.exchange;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "class")
public class XmlNetClass {

	@XmlElementWrapper(name = "members")
	@XmlElement(name = "member")
	public XmlNetMember[] members;
}
