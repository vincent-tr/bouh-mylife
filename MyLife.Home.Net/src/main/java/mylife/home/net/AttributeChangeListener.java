package mylife.home.net;

import mylife.home.net.structure.NetAttribute;

/**
 * Listener de valeur chang�e sur attribut
 * @author pumbawoman
 *
 */
public interface AttributeChangeListener {

	void attributeChanged(NetObject obj, NetAttribute attribute, Object value);
	
}
