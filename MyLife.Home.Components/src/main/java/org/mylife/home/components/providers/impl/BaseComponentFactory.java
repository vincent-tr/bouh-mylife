package org.mylife.home.components.providers.impl;

import org.mylife.home.components.providers.Component;
import org.mylife.home.components.providers.ComponentFactory;

public class BaseComponentFactory implements ComponentFactory {

	private final Class<?> componentClass;

	protected BaseComponentFactory(Class<?> componentClass) {
		this.componentClass = componentClass;
	}

	@Override
	public String getType() {
		return componentClass.getSimpleName();
	}

	@Override
	public String getDisplayType() {
		return getType();
	}

	@Override
	public Component create() throws Exception {
		return (Component) componentClass.newInstance();
	}

}
