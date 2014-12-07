package org.mylife.home.net.hub.web.model;

import java.util.Collection;

public class ChannelView {

	private final String name;
	private final Collection<String> users;
	
	public ChannelView(String name, Collection<String> users) {
		this.name = name;
		this.users = users;
	}

	public String getName() {
		return name;
	}

	public Collection<String> getUsers() {
		return users;
	}
	
}
