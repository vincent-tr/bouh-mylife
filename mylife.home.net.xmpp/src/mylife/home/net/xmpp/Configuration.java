package mylife.home.net.xmpp;

import aQute.bnd.annotation.metatype.Meta;

/**
 * Classe immuable de configuration
 * @author pumbawoman
 */
@Meta.OCD(name="Mylife.Home Net XMPP")
interface Configuration {

	/**
	 * Serveur
	 */
	@Meta.AD(name="XMPP server", deflt="files.mti-team2.dyndns.org", required=false)
	String xmppServer();
	
	/**
	 * muc
	 */
	@Meta.AD(name="MUC room", deflt="home@conference.mti-team2.dyndns.org", required=false)
	String mucRoom();
}
