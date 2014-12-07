package org.mylife.home.net.hub.exchange;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ircConfiguration")
public class XmlIrcConfiguration {

	public String networkName;
	public String serverName;
	public int serverToken;
	
	public XmlIrcListener[] listeners;
}
