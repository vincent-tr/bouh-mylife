package org.mylife.home.net;

import org.mylife.home.net.structure.NetAttribute;

/**
 * Listener de valeur chang�e sur attribut
 * @author pumbawoman
 *
 */
public interface AttributeChangeListener {

	void attributeChanged(NetObject obj, NetAttribute attribute, Object value);
	
}
