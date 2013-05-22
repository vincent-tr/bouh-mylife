package mylife.home.irc.server.structure;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Représentation d'un salon 
 * @author pumbawoman
 *
 */
public class Channel {

	/**
	 * Nom du salon
	 */
	private final String name;
	
	/**
	 * Topic
	 */
	private String topic;
	
	/**
	 * Modes du salon
	 */
	private final Collection<Mode> modes = new ArrayList<Mode>();
	
	/**
	 * Utilisateurs présents sur le salon
	 */
	private final Collection<User> users = new ArrayList<User>();

	/**
	 * Contructeur avec nom
	 * @param name
	 */
	public Channel(String name) {
		if(name == null || name.length() == 0)
			throw new IllegalArgumentException("Channel name can not be null");
		this.name = name;
	}
	
	/**
	 * Nom du salon
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Topic
	 * @return
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * Topic
	 * @param topic
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * Modes du salon
	 * @return
	 */
	public Collection<Mode> getModes() {
		return modes;
	}

	/**
	 * Utilisateurs présents sur le salon
	 * @return
	 */
	public Collection<User> getUsers() {
		return users;
	}

	@Override
	public int hashCode() {
		return name.toLowerCase().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Channel))
			return false;
		Channel other = (Channel)obj;
		return this.hashCode() == other.hashCode(); 
	}
}
