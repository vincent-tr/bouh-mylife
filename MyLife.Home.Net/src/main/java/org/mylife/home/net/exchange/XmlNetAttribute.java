package org.mylife.home.net.exchange;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "attribute")
public class XmlNetAttribute extends XmlNetMember {

	public XmlNetType type;
}
