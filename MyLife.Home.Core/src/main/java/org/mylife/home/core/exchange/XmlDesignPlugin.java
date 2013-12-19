package org.mylife.home.core.exchange;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * Représentation des données de design d'un plugin
 * 
 * @author pumbawoman
 * 
 */
public class XmlDesignPlugin {

	public String type;
	public String displayType;
	
	public byte[] image;
	
	@XmlElementWrapper(name = "configuration")
	@XmlElement(name = "configurationItem")
	public XmlDesignConfiguration[] configuration;
	
	@XmlElementWrapper(name = "attributes")
	@XmlElement(name = "attribute")
	public XmlDesignAttribute[] attributes;
	
	@XmlElementWrapper(name = "actions")
	@XmlElement(name = "action")
	public XmlDesignAction[] actions;
}
