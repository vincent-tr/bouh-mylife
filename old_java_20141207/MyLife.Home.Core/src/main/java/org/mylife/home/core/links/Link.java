package org.mylife.home.core.links;

import org.mylife.home.net.AttributeChangeListener;
import org.mylife.home.net.ConnectedChangeListener;
import org.mylife.home.net.NetContainer;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.structure.NetAttribute;
import org.mylife.home.net.structure.NetType;

/**
 * Lien entre 2 objets
 * 
 * @author pumbawoman
 * 
 */
public class Link implements AttributeChangeListener, ConnectedChangeListener {

	private final NetContainer sourceContainer;
	private final NetContainer targetContainer;

	private final String sourceAttributeName;
	private final String targetActionName;

	private final NetType linkType;
	private final boolean parameterlessAction;

	public Link(NetContainer sourceContainer, NetContainer targetContainer,
			String sourceAttributeName, String targetActionName, NetType linkType, boolean parameterlessAction) {

		this.sourceContainer = sourceContainer;
		this.targetContainer = targetContainer;
		this.sourceAttributeName = sourceAttributeName;
		this.targetActionName = targetActionName;
		this.linkType = linkType;
		this.parameterlessAction = parameterlessAction;

		// Enregistrement des listeners
		if (!sourceContainer.isLocal())
			sourceContainer.registerConnectedChange(this);
		if (!targetContainer.isLocal())
			targetContainer.registerConnectedChange(this);
		sourceContainer.getObject().registerAttributeChange(
				sourceAttributeName, this);
	}

	public void close() {
		if (!sourceContainer.isLocal())
			sourceContainer.unregisterConnectedChange(this);
		if (!targetContainer.isLocal())
			targetContainer.unregisterConnectedChange(this);
		sourceContainer.getObject().unregisterAttributeChange(
				sourceAttributeName, this);
	}

	private Object getAttributeValue() {
		// si l'objet est déconnecté alors sa valeur sera déjà null
		return sourceContainer.getObject().getAttributeValue(
				sourceAttributeName);
	}

	private void executeTargetMethod(Object value) {

		// on exécute que si l'objet cible n'est pas distant et déconnecté
		if (!targetContainer.isLocal() && !targetContainer.isConnected())
			return;

		Object[] args = null;
		if (parameterlessAction)
			args = new Object[0];
		else
			args = new Object[] { value };
		targetContainer.getObject().executeAction(targetActionName, args);
	}

	@Override
	public void connectedChanged(NetContainer container, boolean isConnected) {
		// Si l'objet source devient connecté, alors ses valeurs vont être
		// obtenues et changés (elles sont mises à null sur déconnexion)
		// donc rien à faire

		// Si l'objet cible est déconnecté on ne peut plus appeler sa méthode
		// donc rien à faire
		// Si l'objet cible est connecté et distant, alors on appelle sa méthode
		// comme après un changement de propriété
		if (container == targetContainer && isConnected
				&& !targetContainer.isLocal())
			executeTargetMethod(getAttributeValue());
	}

	@Override
	public void attributeChanged(NetObject obj, NetAttribute attribute,
			Object value) {

		// Sur changement d'attribut on appelle la méthode
		executeTargetMethod(value);
	}

	public NetContainer getSourceContainer() {
		return sourceContainer;
	}

	public NetContainer getTargetContainer() {
		return targetContainer;
	}

	public String getSourceAttributeName() {
		return sourceAttributeName;
	}

	public String getTargetMethodName() {
		return targetActionName;
	}

	public NetType getLinkType() {
		return linkType;
	}

	public boolean isParameterlessAction() {
		return parameterlessAction;
	}
}
