package org.mylife.home.net;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration
 * @author pumbawoman
 *
 */
public final class Configuration {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(Configuration.class.getName());

	private static final Configuration instance = new Configuration();
	
	public static Configuration getInstance() {
		return instance;
	}
	
	private final Properties props;
	
	private Configuration() {
		props = new Properties();
		try {
			props.load(this.getClass().getClassLoader().getResourceAsStream("net.properties"));
		} catch (IOException e) {
			log.log(Level.SEVERE, "Configuration read error", e);
		}
	}
	
	public String getProperty(String key) {
		return props.getProperty(key);
	}
	
	public Properties getProperties() {
		return props;
	}
}
