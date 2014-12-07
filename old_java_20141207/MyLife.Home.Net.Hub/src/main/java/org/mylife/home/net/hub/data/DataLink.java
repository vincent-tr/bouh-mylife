package org.mylife.home.net.hub.data;

/**
 * Donnée d'un lien
 * 
 * @author pumbawoman
 * 
 */
public class DataLink {

	private int id;
	private String name;
	private String address;
	private int port;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
