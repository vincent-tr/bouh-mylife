package org.mylife.home.ui.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.exchange.ui.XmlUiAction;
import org.mylife.home.net.exchange.ui.XmlUiComponent;
import org.mylife.home.net.exchange.ui.XmlUiCoreAction;
import org.mylife.home.net.exchange.ui.XmlUiDynamicIcon;
import org.mylife.home.net.exchange.ui.XmlUiEnumIcon;
import org.mylife.home.net.exchange.ui.XmlUiEnumIconMapping;
import org.mylife.home.net.exchange.ui.XmlUiIcon;
import org.mylife.home.net.exchange.ui.XmlUiRangeIcon;
import org.mylife.home.net.exchange.ui.XmlUiRangeIconMapping;
import org.mylife.home.net.exchange.ui.XmlUiStaticIcon;
import org.mylife.home.net.exchange.ui.XmlUiWindowAction;
import org.mylife.home.net.structure.NetAttribute;
import org.mylife.home.ui.services.ServiceAccess;

/**
 * Représentation d'un composant
 * 
 * @author pumbawoman
 * 
 */
public class Component {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(Component.class
			.getName());

	public static final int ACTION_PRIMARY = 1;
	public static final int ACTION_SECONDARY = 2;

	private final String id;
	private final Window owner;
	private final double x;
	private final double y;
	private final Map<String, Boolean> onlineStatus = new HashMap<String, Boolean>();
	private final Map<MappingChecker, String> icons = new HashMap<MappingChecker, String>();
	private final Collection<String> dependencies;
	private String currentIcon;
	private final String imageObjectId;
	private final String imageAttributeName;
	private final Action primaryAction;
	private final Action secondaryAction;

	/* internal */Component(XmlUiComponent source, Window owner) {
		Validate.notEmpty(source.id);
		this.id = source.id;
		this.owner = owner;
		x = source.positionX;
		y = source.positionY;

		Pair<String, String> iconObject = buildIcon(source.icon);

		this.imageObjectId = iconObject.getLeft();
		this.imageAttributeName = iconObject.getRight();

		if (icons.size() == 0) {
			icons.put(new StaticMappingChecker(), null);
		}

		primaryAction = buildAction(source.primaryAction);
		secondaryAction = buildAction(source.secondaryAction);

		Collection<String> dependencies = new ArrayList<String>();
		dependencies.addAll(onlineStatus.keySet());
		if (!StringUtils.isEmpty(imageObjectId))
			dependencies.add(imageObjectId);
		this.dependencies = Collections.unmodifiableCollection(dependencies);
	}

	private Pair<String, String> buildIcon(XmlUiIcon xmlIcon) {
		String imageObjectId = null;
		String imageAttributeName = null;

		if (xmlIcon == null) {
			// rien à faire, sera traité avec la taille de la map à 0
		} else if (xmlIcon instanceof XmlUiStaticIcon) {
			icons.put(new StaticMappingChecker(),
					((XmlUiStaticIcon) xmlIcon).iconId);
		} else if (xmlIcon instanceof XmlUiDynamicIcon) {

			if (xmlIcon instanceof XmlUiEnumIcon) {
				XmlUiEnumIconMapping[] mappings = ((XmlUiEnumIcon) xmlIcon).mappings;
				if (mappings != null) {
					for (XmlUiEnumIconMapping mapping : mappings) {
						Validate.notBlank(mapping.enumValue);
						icons.put(new EnumMappingChecker(mapping.enumValue),
								mapping.iconId);
					}
				}

			} else if (xmlIcon instanceof XmlUiRangeIcon) {
				XmlUiRangeIconMapping[] mappings = ((XmlUiRangeIcon) xmlIcon).mappings;
				if (mappings != null) {
					for (XmlUiRangeIconMapping mapping : mappings) {
						Validate.isTrue(mapping.minValue <= mapping.maxValue);
						icons.put(new RangeMappingChecker(mapping.minValue,
								mapping.maxValue), mapping.iconId);
					}
				}

			} else {
				throw new UnsupportedOperationException("Unknown icon type");
			}

			XmlUiDynamicIcon xmlDynamicIcon = (XmlUiDynamicIcon) xmlIcon;

			if (icons.size() > 0) {
				imageObjectId = xmlDynamicIcon.componentId;
				imageAttributeName = xmlDynamicIcon.componentAttribute;
				if (StringUtils.isEmpty(imageObjectId))
					throw new UnsupportedOperationException(
							"DynamicIcon with mapping but without componentId");
				if (StringUtils.isEmpty(imageAttributeName))
					throw new UnsupportedOperationException(
							"DynamicIcon with mapping but without attributeName");

			}

			// Icone par défaut à la fin si besoin
			String defaultIcon = xmlDynamicIcon.defaultIconId;
			if (!StringUtils.isEmpty(defaultIcon))
				icons.put(new StaticMappingChecker(), defaultIcon);

		} else {
			throw new UnsupportedOperationException("Unknown icon type");
		}

		return Pair.of(imageObjectId, imageAttributeName);
	}

	private Action buildAction(XmlUiAction source) {
		if (source == null)
			return null;

		if (source instanceof XmlUiWindowAction) {
			XmlUiWindowAction windowAction = (XmlUiWindowAction) source;
			Validate.notEmpty(windowAction.windowId);

			return new WindowAction(windowAction.windowId, windowAction.popup);
		}

		if (source instanceof XmlUiCoreAction) {
			XmlUiCoreAction coreAction = (XmlUiCoreAction) source;
			Validate.notEmpty(coreAction.componentId);
			Validate.notEmpty(coreAction.componentAction);

			onlineStatus.put(coreAction.componentId, false);
			return new CoreAction(coreAction.componentId,
					coreAction.componentAction);
		}

		throw new UnsupportedOperationException("Unknown action type");
	}

	public String getId() {
		return id;
	}

	public Window getOwner() {
		return owner;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public Action getPrimaryAction() {
		return primaryAction;
	}

	public Action getSecondaryAction() {
		return secondaryAction;
	}

	/**
	 * Exécution de l'action (uniquement si action de type core)
	 */
	public void actionExecuteCore(int actionType) {
		switch (actionType) {
		case ACTION_PRIMARY:
			actionExecuteCore(primaryAction);
			break;

		case ACTION_SECONDARY:
			actionExecuteCore(secondaryAction);
			break;

		default:
			throw new UnsupportedOperationException("Unknown action type");
		}
	}

	private void actionExecuteCore(Action action) {
		if (action == null || !(action instanceof CoreAction)) {
			log.warning("Unable to execute core action : bad action type");
			return;
		}

		CoreAction coreAction = (CoreAction) action;
		NetObject obj = ServiceAccess.getInstance().getNetService().getNetObject(coreAction.getComponentId());
		if(obj == null) {
			log.warning("Unable to execute core action : object not found");
		}
		
		obj.executeAction(coreAction.getComponentAction(), EMPTY_ARGUMENTS);
	}
	
	private static final Object[] EMPTY_ARGUMENTS = {};

	/**
	 * Obtention des ids des NetObjects dépendants
	 * 
	 * @return
	 */
	public Collection<String> getDependencies() {
		return dependencies;
	}

	/**
	 * Reservé pour DispatcherService
	 * 
	 * @param obj
	 */
	public void objectOnlineChanged(NetObject obj, boolean online) {
		if (!onlineStatus.containsKey(obj.getId()))
			return; // Ne devrait pas se produire

		boolean oldOnline = isOnline();

		onlineStatus.put(obj.getId(), online);

		boolean newOnline = isOnline();
		if (oldOnline != newOnline)
			ServiceAccess.getInstance().getDispatcherService()
					.componentOnlineChanged(this, newOnline);
	}

	public boolean isOnline() {
		for (Boolean online : onlineStatus.values()) {
			// Si un n'est pas connecté alors on n'est pas connecté
			if (online == null || !online)
				return false;
		}
		return true;
	}

	/**
	 * Reservé pour DispatcherService
	 * 
	 * @param obj
	 * @param attribute
	 * @param newValue
	 */
	public void objectAttributeChanged(NetObject obj, NetAttribute attribute,
			Object newValue) {

		if (!obj.getId().equals(imageObjectId))
			return;
		if (!attribute.getName().equals(imageAttributeName))
			return;

		String newIcon = null;
		for (Map.Entry<MappingChecker, String> item : icons.entrySet()) {
			if (item.getKey().check(newValue)) {
				newIcon = item.getValue();
			}
		}
		if (StringUtils.equals(newIcon, currentIcon))
			return;

		currentIcon = newIcon;
		ServiceAccess.getInstance().getDispatcherService()
				.componentIconChanged(this, currentIcon);
	}

	public String getIconId() {
		return currentIcon;
	}

	public Collection<String> getIcons() {
		return Collections.unmodifiableCollection(icons.values());
	}

	/**
	 * Verification d'un mapping pour une valeur
	 * 
	 * @author pumbawoman
	 * 
	 */
	private static interface MappingChecker {
		/**
		 * Vérifie si une valeur est valide pour le mapping
		 * 
		 * @param value
		 * @return
		 */
		public boolean check(Object value);
	}

	private static class StaticMappingChecker implements MappingChecker {
		@Override
		public boolean check(Object value) {
			return true;
		}
	}

	private static class EnumMappingChecker implements MappingChecker {

		private final String value;

		public EnumMappingChecker(String value) {
			this.value = value;
		}

		@Override
		public boolean check(Object value) {
			if (value == null)
				return false;
			return this.value.equals(value);
		}
	}

	private static class RangeMappingChecker implements MappingChecker {

		private final int min;
		private final int max;

		public RangeMappingChecker(int min, int max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public boolean check(Object value) {
			Integer intValue = (Integer) value;
			if (intValue == null)
				return false;
			return intValue >= min && intValue <= max;
		}
	}
}
