package org.mylife.home.net.hub.web.model;

import java.util.Collection;

public class UserView {

	private final String name;
	private final Collection<String> channels;

	public UserView(String name, Collection<String> channels) {
		this.name = name;
		this.channels = channels;
	}

	public String getName() {
		return name;
	}

	public Collection<String> getChannels() {
		return channels;
	}

}
