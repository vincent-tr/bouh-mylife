package org.mylife.home.core.plugins.enhanced;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.mylife.home.core.plugins.PluginContext;
import org.mylife.home.core.plugins.enhanced.annotations.PluginDataName;

/**
 * Gestion du wrapper de configuration
 * 
 * @author pumbawoman
 * 
 */
class PluginConfigurationWrapper implements InvocationHandler {

	private final PluginContext context;
	private final Object configuration;

	public PluginConfigurationWrapper(PluginContext context,
			Class<?> configurationInterface) {
		this.context = context;
		configuration = Proxy.newProxyInstance(
				configurationInterface.getClassLoader(),
				new Class[] { configurationInterface }, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

		String name = null;
		PluginDataName nameAnnotation = method
				.getAnnotation(PluginDataName.class);
		if (nameAnnotation != null)
			name = nameAnnotation.name();
		else
			name = method.getName();

		Map<String, String> config = context.getConfiguration();
		String valueString = config.get(name);

		Class<?> returnClass = method.getReturnType();
		if (returnClass.equals(String.class))
			return valueString;
		if (returnClass.isEnum())
			return Helpers.valueOfEnum(returnClass, valueString);
		if(returnClass.isPrimitive())
			return valueOfPrimitive(returnClass, valueString);
		return null;
	}

	private Object valueOfPrimitive(Class<?> clazz, String value) {
		if (boolean.class == clazz)
			return Boolean.parseBoolean(value);
		if (byte.class == clazz)
			return Byte.parseByte(value);
		if (short.class == clazz)
			return Short.parseShort(value);
		if (int.class == clazz)
			return Integer.parseInt(value);
		if (long.class == clazz)
			return Long.parseLong(value);
		if (float.class == clazz)
			return Float.parseFloat(value);
		if (double.class == clazz)
			return Double.parseDouble(value);
		return null;
	}

	public Object getConfiguration() {
		return configuration;
	}

}
