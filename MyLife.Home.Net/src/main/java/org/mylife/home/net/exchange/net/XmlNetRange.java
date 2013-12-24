package org.mylife.home.net.exchange.net;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "range")
public class XmlNetRange extends XmlNetType {

	public int min;
	public int max;
}
