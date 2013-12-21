package org.mylife.home.core.plugins.impl.ui;

import org.mylife.home.core.plugins.enhanced.Attribute;
import org.mylife.home.core.plugins.enhanced.EnhancedPluginFactory;
import org.mylife.home.core.plugins.enhanced.annotations.Plugin;
import org.mylife.home.core.plugins.enhanced.annotations.PluginAction;
import org.mylife.home.core.plugins.enhanced.annotations.PluginAttribute;
import org.mylife.home.core.plugins.impl.Types;

@Plugin(displayType = "Lumière", ui = true)
public class Light {

	/**
	 * Fabrique
	 * 
	 * @author pumbawoman
	 * 
	 */
	public static class Factory extends EnhancedPluginFactory {
		public Factory() throws Exception {
			super(Light.class);
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
		this.value.setValue(value);
	}

}
