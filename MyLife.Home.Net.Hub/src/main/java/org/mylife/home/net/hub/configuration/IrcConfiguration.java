package org.mylife.home.net.hub.configuration;

import java.util.Collection;

/**
 * Interface à implémenter pour configuration
 * @author trumpffv
 *
 */
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
	Collection<IrcBinding> getBindings();
	Collection<IrcOperator> getOperators();
}
