package org.mylife.home.core.exchange.core;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * Composant du core
 * @author pumbawoman
 *
 */
public class XmlCoreComponent {

	public String id;
	public String pluginType;
	
	@XmlElementWrapper(name = "configuration")
	@XmlElement(name = "configurationItem")
	public XmlCoreComponentConfigurationItem[] configuration;
	
	public double designX;
	public double designY;
}
