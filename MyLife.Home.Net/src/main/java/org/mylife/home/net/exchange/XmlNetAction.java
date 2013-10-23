package org.mylife.home.net.exchange;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "action")
public class XmlNetAction extends XmlNetMember {

	public XmlNetType[] arguments;
}
