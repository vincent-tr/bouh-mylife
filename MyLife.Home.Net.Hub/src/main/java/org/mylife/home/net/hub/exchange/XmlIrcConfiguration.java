package org.mylife.home.net.hub.exchange;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ircConfiguration")
public class XmlIrcConfiguration {

	public String networkName;
	public String serverName;
	public String serverDescription;
	public int serverToken;
	public int pingIntervalMs;
	public int pingTimeoutMs;
	public String location1;
	public String location2;
	public String email;
	public String serverInfoContent;
	public String serverMotdContent;
	
	public XmlIrcBinding[] bindings;
	public XmlIrcOperator[] operators;
}
