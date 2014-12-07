package org.mylife.home.components.providers.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.mylife.home.components.providers.Component;
import org.mylife.home.components.providers.ComponentFactory;

public class BaseComponentFactory implements ComponentFactory {

	private final Class<? extends Component> componentClass;
	private final Collection<String> parameterNames;

	protected BaseComponentFactory(Class<? extends Component> componentClass,
			String... parameterNames) {
		this.componentClass = componentClass;
		this.parameterNames = Collections.unmodifiableCollection(Arrays
				.asList(parameterNames));
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
		return componentClass.newInstance();
	}

	/**
	 * Obtention si supporté de la liste des noms de paramètres supportés
	 * 
	 * @return
	 */
	public Collection<String> getParameterNames() {
		return parameterNames;
	}

}
