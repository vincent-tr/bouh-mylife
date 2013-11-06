package org.mylife.home.net.hub.configuration;

import java.util.Collection;

public interface IrcConfiguration {

	String getNetworkName();
	String getServerName();
	String getServerDescription();
	int getServerToken();
	int getPingIntervalMs();
	int getPingTimeoutMs();
	String getLocation1();
	String getLocation2();
	String getEmail();
	String getServerInfoContent();
	String getServerMotdContent();
	Collection<IrcLinkAccept> getLinksAccept();
	Collection<IrcLinkConnect> getLinksConnect();
	Collection<IrcBinding> getBindings();
	Collection<IrcOperator> getOperators();
}
