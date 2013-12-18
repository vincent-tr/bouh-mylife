package org.mylife.home.core.plugins.enhanced;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mylife.home.core.plugins.design.PluginDesignAction;
import org.mylife.home.core.plugins.design.PluginDesignAttribute;
import org.mylife.home.core.plugins.design.PluginDesignConfiguration;
import org.mylife.home.core.plugins.design.PluginDesignMetadata;
import org.mylife.home.core.plugins.enhanced.annotations.PluginDataName;
import org.mylife.home.core.plugins.enhanced.annotations.PluginMandatory;
import org.mylife.home.core.plugins.enhanced.annotations.PluginPossibleValues;
import org.mylife.home.core.plugins.enhanced.metadata.ActionMetadata;
import org.mylife.home.core.plugins.enhanced.metadata.AttributeMetadata;
import org.mylife.home.core.plugins.enhanced.metadata.MemberMetadata;
import org.mylife.home.core.plugins.enhanced.metadata.PluginClassMetadata;

/**
 * Implémentation des métadonnées de design
 * 
 * @author pumbawoman
 * 
 */
class EnhancedPluginDesignMetadata implements PluginDesignMetadata {

	private final byte[] image;
	private final Collection<PluginDesignConfiguration> configuration;
	private final Collection<PluginDesignAttribute> attributes;
	private final Collection<PluginDesignAction> actions;

	public EnhancedPluginDesignMetadata(PluginClassMetadata metadata) {
		image = metadata.getImage();
		configuration = buildConfigurationMetadata(metadata
				.getConfigurationInterface());

		Pair<Collection<PluginDesignAttribute>, Collection<PluginDesignAction>> data = buildMembers(metadata
				.getMembers());
		attributes = data.getLeft();
		actions = data.getRight();
	}

	private Collection<PluginDesignConfiguration> buildConfigurationMetadata(
			Class<?> configurationInterface) {

		if (configurationInterface == null)
			return null;

		Collection<PluginDesignConfiguration> list = new ArrayList<PluginDesignConfiguration>();
		for (Method method : configurationInterface.getMethods()) {
			String name = null;
			PluginDataName nameAnnotation = method
					.getAnnotation(PluginDataName.class);
			if (nameAnnotation != null)
				name = nameAnnotation.name();
			else
				name = method.getName();

			String displayName = null;
			if (nameAnnotation != null)
				displayName = nameAnnotation.displayName();
			if (StringUtils.isEmpty(displayName))
				displayName = name;

			boolean mandatory = false;
			PluginMandatory mandatoryAnnotation = method
					.getAnnotation(PluginMandatory.class);
			if (mandatoryAnnotation != null)
				mandatory = true;

			Collection<Object> values = null;
			PluginPossibleValues valuesAnnotation = method
					.getAnnotation(PluginPossibleValues.class);
			if (valuesAnnotation != null) {
				values = new ArrayList<Object>();
				for (String value : valuesAnnotation.values())
					values.add(value);
				values = Collections.unmodifiableCollection(values);
			}

			list.add(new PluginDesignConfiguration(name, displayName, method
					.getReturnType(), mandatory, values));
		}

		return Collections.unmodifiableCollection(list);
	}

	private Pair<Collection<PluginDesignAttribute>, Collection<PluginDesignAction>> buildMembers(
			Collection<MemberMetadata> members) {

		Collection<PluginDesignAttribute> attributes = new ArrayList<PluginDesignAttribute>();
		Collection<PluginDesignAction> actions = new ArrayList<PluginDesignAction>();

		for (MemberMetadata member : members) {
			if (member instanceof AttributeMetadata) {
				AttributeMetadata attribute = (AttributeMetadata) member;
				attributes.add(new PluginDesignAttribute(attribute.getName(),
						attribute.getDisplayName(), attribute.getNetType()));
			}

			if (member instanceof ActionMetadata) {
				ActionMetadata action = (ActionMetadata) member;
				actions.add(new PluginDesignAction(action.getName(), action
						.getDisplayName(), action.getNetTypes()));
			}
		}

		return Pair.of(Collections.unmodifiableCollection(attributes),
				Collections.unmodifiableCollection(actions));
	}

	@Override
	public byte[] getImage() {
		return image;
	}

	@Override
	public Collection<PluginDesignConfiguration> getConfigurationData() {
		return configuration;
	}

	@Override
	public Collection<PluginDesignAttribute> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<PluginDesignAction> getActions() {
		return actions;
	}

}
