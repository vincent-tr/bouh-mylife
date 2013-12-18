package org.mylife.home.core.plugins.enhanced;

import org.mylife.home.core.plugins.enhanced.metadata.AttributeMetadata;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.structure.NetAttribute;
import org.mylife.home.net.structure.NetMember;

/**
 * Gestion d'un attribut
 * 
 * @author pumbawoman
 * 
 */
class AttributeWrapper extends MemberWrapper implements
		Attribute.ChangeListener {

	// private final Object pluginInstance;
	private final AttributeMetadata metadata;
	private final Attribute<?> attribute;
	private NetObject netObject;

	public AttributeWrapper(Object pluginInstance, AttributeMetadata metadata)
			throws Exception {

		// this.pluginInstance = pluginInstance;
		this.metadata = metadata;

		// Exécution du getter du plugin pour récupérer l'attribut
		attribute = (Attribute<?>) metadata.getMethod().invoke(pluginInstance);
	}

	/**
	 * Création du membre
	 * 
	 * @return
	 */
	@Override
	public NetMember createMember() {
		return new NetAttribute(metadata.getIndex(), metadata.getName(),
				metadata.getNetType());
	}

	/**
	 * Binding sur le NetObject
	 * 
	 * @param netObject
	 */
	@Override
	public synchronized void bind(NetObject netObject) {
		this.netObject = netObject;
		attribute.addListener(this);

		// Définition de la valeur initiale
		attributeChanged(attribute, attribute.getValue());
	}

	/**
	 * Suppression du binding
	 * 
	 * @param netObject
	 */
	@Override
	public synchronized void unbind(NetObject netObject) {
		this.netObject = null;
		attribute.removeListener(this);
	}

	/**
	 * Changement de valeur de l'attribut
	 */
	@Override
	public synchronized void attributeChanged(Attribute<?> owner, Object value) {

		Object netValue = toNetValue(value);
		netObject.setAttributeValue(metadata.getName(), netValue);
	}
}
