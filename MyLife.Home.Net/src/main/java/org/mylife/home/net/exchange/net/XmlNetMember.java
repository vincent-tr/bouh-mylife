package org.mylife.home.net.exchange.net;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "member")
@XmlSeeAlso({ XmlNetAction.class, XmlNetAttribute.class })
public class XmlNetMember {

	public int index;
	public String name;
}
