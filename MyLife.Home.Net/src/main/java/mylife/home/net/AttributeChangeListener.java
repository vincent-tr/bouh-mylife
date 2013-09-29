package mylife.home.net;

import mylife.home.net.structure.NetAttribute;

/**
 * Listener de valeur changée sur attribut
 * @author pumbawoman
 *
 */
public interface AttributeChangeListener {

	void attributeChanged(NetObject obj, NetAttribute attribute, Object value);
	
}
