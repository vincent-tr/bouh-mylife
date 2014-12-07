package org.mylife.home.core.plugins.impl;

import org.mylife.home.core.plugins.enhanced.Attribute;
import org.mylife.home.core.plugins.enhanced.EnhancedPluginFactory;
import org.mylife.home.core.plugins.enhanced.annotations.Plugin;
import org.mylife.home.core.plugins.enhanced.annotations.PluginAction;
import org.mylife.home.core.plugins.enhanced.annotations.PluginAttribute;

/**
 * Implémentation d'un va-et-vient
 * 
 * @author pumbawoman
 * 
 */
@Plugin(displayType = "Va-et-vient")
public class TwoWaySwitch {

	/**
	 * Fabrique
	 * 
	 * @author pumbawoman
	 * 
	 */
	public static class Factory extends EnhancedPluginFactory {
		public Factory() throws Exception {
			super(TwoWaySwitch.class);
		}
	}

	private final Attribute<Types.Boolean> value = new Attribute<Types.Boolean>(
			Types.Boolean.off);


	@PluginAttribute(index = 0)
	public Attribute<Types.Boolean> output() {
		return value;
	}

	@PluginAction(index = 1)
	public void input(Types.Boolean value) {
		switchValue();
	}

	private synchronized void switchValue() {
		Types.Boolean val = value.getValue();
		val = val.equals(Types.Boolean.on) ? Types.Boolean.off
				: Types.Boolean.on;
		value.setValue(val);
	}
}
