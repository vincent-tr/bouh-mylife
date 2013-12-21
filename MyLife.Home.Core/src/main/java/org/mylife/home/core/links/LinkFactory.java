package org.mylife.home.core.links;

import java.util.List;

import org.mylife.home.core.exchange.core.XmlCoreLink;
import org.mylife.home.net.NetContainer;
import org.mylife.home.net.NetRepository;
import org.mylife.home.net.structure.NetAction;
import org.mylife.home.net.structure.NetAttribute;
import org.mylife.home.net.structure.NetClass;
import org.mylife.home.net.structure.NetMember;
import org.mylife.home.net.structure.NetType;

/**
 * Fabrique de liens
 * 
 * @author pumbawoman
 * 
 */
public class LinkFactory {

	private static final LinkFactory instance = new LinkFactory();

	public static LinkFactory getInstance() {
		return instance;
	}

	private LinkFactory() {

	}

	private static class Check {
		public NetType linkType;
		public boolean parameterlessAction;
	}

	public Link createFromXml(XmlCoreLink xml) {

		NetContainer sourceContainer = NetRepository
				.getObjectById(xml.sourceComponent);
		if (sourceContainer == null) {
			throw new UnsupportedOperationException(String.format(
					"Source component not found : '%s'", xml.sourceComponent));
		}
		String sourceAttributeName = xml.sourceAttribute;

		NetContainer targetContainer = NetRepository
				.getObjectById(xml.targetComponent);
		if (targetContainer == null) {
			throw new UnsupportedOperationException(String.format(
					"Target component not found : '%s'", xml.targetComponent));
		}
		String targetActionName = xml.targetAction;

		Check check = checkLink(sourceContainer, targetContainer,
				sourceAttributeName, targetActionName);

		return new Link(sourceContainer, targetContainer, sourceAttributeName,
				targetActionName, check.linkType, check.parameterlessAction);
	}

	/**
	 * Vérification de la validité du lien
	 * 
	 * @param sourceContainer
	 * @param targetContainer
	 * @param sourceAttributeName
	 * @param targetActionName
	 * @return
	 */
	private Check checkLink(NetContainer sourceContainer,
			NetContainer targetContainer, String sourceAttributeName,
			String targetActionName) {
		NetMember sourceMember = findMember(sourceContainer.getObject()
				.getNetClass(), sourceAttributeName);
		if (sourceMember == null || !(sourceMember instanceof NetAttribute))
			throw new UnsupportedOperationException(
					"Source attribute not found or bad type");
		NetAttribute sourceAttribute = (NetAttribute) sourceMember;

		NetMember targetMember = findMember(targetContainer.getObject()
				.getNetClass(), targetActionName);
		if (targetMember == null || !(targetMember instanceof NetAction))
			throw new UnsupportedOperationException(
					"Target action not found or bad type");
		NetAction targetAction = (NetAction) targetMember;

		Check ret = new Check();
		ret.linkType = sourceAttribute.getType();

		List<NetType> targetArguments = targetAction.getArguments();
		if (targetArguments.size() > 1)
			throw new UnsupportedOperationException(
					"Target action has more than 1 argument");
		else if (targetArguments.size() == 0)
			ret.parameterlessAction = true;
		else {
			// un paramètre
			ret.parameterlessAction = false;
			if (!ret.linkType.equals(targetArguments.get(0)))
				throw new UnsupportedOperationException(
						"Target action argument type not equals to source attribute type");
		}

		return ret;
	}

	private NetMember findMember(NetClass clazz, String name) {
		for (NetMember member : clazz.getMembers()) {
			if (name.equals(member.getName())) {
				return member;
			}
		}
		return null;
	}
}
