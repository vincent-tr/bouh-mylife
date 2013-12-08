package org.mylife.home.net.hub.web.model;

import java.util.Collection;

public class ServerView {

	private final String name;
	private final Collection<UserView> users;
	private final Collection<ServerView> children;

	public ServerView(String name, Collection<UserView> users,
			Collection<ServerView> children) {
		this.name = name;
		this.users = users;
		this.children = children;
	}

	public String getName() {
		return name;
	}

	public Collection<UserView> getUsers() {
		return users;
	}

	public Collection<ServerView> getChildren() {
		return children;
	}

}
