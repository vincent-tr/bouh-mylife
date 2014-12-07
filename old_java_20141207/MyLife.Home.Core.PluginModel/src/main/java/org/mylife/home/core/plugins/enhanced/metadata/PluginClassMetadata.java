package org.mylife.home.core.plugins.enhanced.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.mylife.home.core.plugins.PluginContext;
import org.mylife.home.core.plugins.enhanced.Attribute;
import org.mylife.home.core.plugins.enhanced.Helpers;
import org.mylife.home.core.plugins.enhanced.InvalidPluginException;
import org.mylife.home.core.plugins.enhanced.annotations.Plugin;
import org.mylife.home.core.plugins.enhanced.annotations.PluginAction;
import org.mylife.home.core.plugins.enhanced.annotations.PluginAttribute;
import org.mylife.home.core.plugins.enhanced.annotations.PluginConfiguration;
import org.mylife.home.core.plugins.enhanced.annotations.PluginDestroy;
import org.mylife.home.core.plugins.enhanced.annotations.PluginInit;
import org.mylife.home.core.plugins.enhanced.annotations.PluginRange;
import org.mylife.home.net.structure.NetEnum;
import org.mylife.home.net.structure.NetRange;
import org.mylife.home.net.structure.NetType;

/**
 * Gestion des métadonnées de la classe du plugin
 * 
 * @author pumbawoman
 * 
 */
public class PluginClassMetadata {

	/**
	 * Logger
	 */
	private final static Logger log = Logger
			.getLogger(PluginClassMetadata.class.getName());

	private final Class<?> pluginClass;
	private final String type;
	private final String displayType;
	private final boolean ui;
	private final byte[] image;
	private final Class<?> configurationInterface;
	private final Collection<Method> initMethods;
	private final Collection<Method> destroyMethods;
	private final Collection<MemberMetadata> members;

	public PluginClassMetadata(Class<?> pluginClass) throws Exception {

		this.pluginClass = pluginClass;
		Plugin pluginAnnotation = initPluginAnnotation();
		type = initType(pluginAnnotation);
		displayType = initDisplayType(pluginAnnotation);
		ui = pluginAnnotation.ui();
		image = initImage(pluginClass, pluginAnnotation);
		Pair<Collection<Method>, Class<?>> initData = initInitMethods();
		initMethods = Collections.unmodifiableCollection(initData.getLeft());
		configurationInterface = initData.getRight();
		destroyMethods = Collections
				.unmodifiableCollection(initDestroyMethods());
		members = Collections.unmodifiableCollection(initMembers());
	}

	private Plugin initPluginAnnotation() throws InvalidPluginException {
		Plugin pluginAnnotation = pluginClass.getAnnotation(Plugin.class);
		if (pluginAnnotation == null)
			throw new InvalidPluginException("No plugin annotation");
		return pluginAnnotation;
	}

	private String initType(Plugin pluginAnnotation) {
		String type = pluginAnnotation.type();
		if (StringUtils.isEmpty(type))
			type = pluginClass.getSimpleName();
		return type;
	}

	private String initDisplayType(Plugin pluginAnnotation) {
		String displayType = pluginAnnotation.displayType();
		if (StringUtils.isEmpty(displayType))
			displayType = type;
		return displayType;
	}

	private byte[] initImage(Class<?> pluginClass, Plugin pluginAnnotation) {
		String name = pluginAnnotation.imageResource();
		if (StringUtils.isEmpty(name))
			return null;
		InputStream stream = pluginClass.getResourceAsStream(name);
		if (stream == null)
			return null;
		try {
			return IOUtils.toByteArray(stream);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error reading plugin image", e);
			return null;
		}
	}

	private void throwInvalidMethod(Method method, String usage)
			throws InvalidPluginException {
		throw new InvalidPluginException("Method '" + method.getName()
				+ "' is invalid for : " + usage);
	}

	private void throwInvalidClass(Class<?> clazz, String usage)
			throws InvalidPluginException {
		throw new InvalidPluginException("Class '" + clazz.getName()
				+ "' is invalid for : " + usage);
	}

	private Pair<Collection<Method>, Class<?>> initInitMethods()
			throws InvalidPluginException {
		Collection<Method> methods = new ArrayList<Method>();
		Class<?> configurationInterface = null;

		for (Method method : pluginClass.getMethods()) {
			PluginInit annotation = method.getAnnotation(PluginInit.class);
			if (annotation == null)
				continue;

			Class<?>[] parameters = method.getParameterTypes();
			for (Class<?> parameter : parameters) {

				// Check si de type PluginContext
				if (parameter.equals(PluginContext.class))
					continue;

				// Check si du type de configuration du plugin
				if (configurationInterface != null) {
					if (parameter.equals(configurationInterface))
						continue;
					else
						throwInvalidMethod(method, "PluginInit");
				}

				if (parameter.isAnnotationPresent(PluginConfiguration.class)) {
					checkValidConfigurationInterface(parameter);
					configurationInterface = parameter;
					continue;
				}

				throwInvalidMethod(method, "PluginInit");
			}

			methods.add(method);
		}

		// Pair.of() foire ...
		return new ImmutablePair<Collection<Method>, Class<?>>(methods,
				configurationInterface);
	}

	private void checkValidConfigurationInterface(
			Class<?> configurationInterface) throws InvalidPluginException {
		if (!configurationInterface.isInterface())
			throwInvalidClass(configurationInterface, "ConfigurationInterface");
		if (!configurationInterface
				.isAnnotationPresent(PluginConfiguration.class))
			throwInvalidClass(configurationInterface, "ConfigurationInterface");

		for (Method method : configurationInterface.getMethods()) {
			if (method.getParameterTypes().length > 0)
				throwInvalidClass(configurationInterface,
						"ConfigurationInterface");
			Class<?> returnClass = method.getReturnType();

			if (returnClass.isPrimitive())
				continue;
			if (returnClass.equals(String.class))
				continue;
			if (returnClass.isEnum())
				continue;

			throwInvalidClass(configurationInterface, "ConfigurationInterface");
		}
	}

	private Collection<Method> initDestroyMethods()
			throws InvalidPluginException {
		Collection<Method> methods = new ArrayList<Method>();

		for (Method method : pluginClass.getMethods()) {
			PluginDestroy annotation = method
					.getAnnotation(PluginDestroy.class);
			if (annotation == null)
				continue;

			Class<?>[] parameters = method.getParameterTypes();
			for (Class<?> parameter : parameters) {

				// Check si de type PluginContext
				if (parameter.equals(PluginContext.class))
					continue;

				throwInvalidMethod(method, "PluginDestroy");
			}

			methods.add(method);
		}
		return methods;
	}

	private Collection<MemberMetadata> initMembers()
			throws InvalidPluginException {
		SortedMap<Integer, MemberMetadata> members = new TreeMap<Integer, MemberMetadata>();
		for (Method method : pluginClass.getMethods()) {
			MemberMetadata member = null;
			PluginAction actionAnnotation = method
					.getAnnotation(PluginAction.class);
			if (actionAnnotation != null)
				member = initAction(method, actionAnnotation);

			PluginAttribute attributeAnnotation = method
					.getAnnotation(PluginAttribute.class);
			if (attributeAnnotation != null)
				member = initAttribute(method, attributeAnnotation);

			if (member == null)
				continue;

			if (members.containsKey(member.getIndex()))
				throw new InvalidPluginException(
						"Member indexes are not unique");
			members.put(member.getIndex(), member);
		}

		return members.values();
	}

	private MemberMetadata initAction(Method method, PluginAction annotation)
			throws InvalidPluginException {

		Class<?>[] parameters = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();
		Collection<NetType> netTypes = new ArrayList<NetType>();
		for (int i = 0; i < parameters.length; i++) {
			netTypes.add(initMemberArgument(parameters[i], annotations[i]));
		}

		String name = annotation.name();
		if (StringUtils.isEmpty(name))
			name = method.getName();

		String displayType = annotation.displayType();
		if (StringUtils.isEmpty(displayType))
			displayType = name;

		return new ActionMetadata(method, annotation.index(), name,
				displayType, netTypes);
	}

	private MemberMetadata initAttribute(Method method,
			PluginAttribute annotation) throws InvalidPluginException {

		// Un attribut ne doit avoir qu'un type de retour, de type Attribute
		if (method.getParameterTypes().length > 0)
			throwInvalidMethod(method, "PluginAttribute");
		Class<?> returnClass = method.getReturnType();
		if (!returnClass.equals(Attribute.class))
			throwInvalidMethod(method, "PluginAttribute");
		Type returnType = method.getGenericReturnType(); // Attribute<Type>
		returnClass = (Class<?>) ((ParameterizedType) returnType)
				.getActualTypeArguments()[0]; // Type
		NetType netType = initMemberArgument(returnClass,
				method.getAnnotations());

		String name = annotation.name();
		if (StringUtils.isEmpty(name))
			name = method.getName();

		String displayType = annotation.displayType();
		if (StringUtils.isEmpty(displayType))
			displayType = name;

		return new AttributeMetadata(method, annotation.index(), name,
				displayType, netType);
	}

	private NetType initMemberArgument(Class<?> clazz, Annotation[] annotations)
			throws InvalidPluginException {

		if (clazz.isEnum())
			return new NetEnum(Helpers.namesOfEnum(clazz));

		if (clazz.equals(Integer.class)) {
			for (Annotation annotation : annotations) {
				if (annotation instanceof PluginRange) {
					PluginRange range = (PluginRange) annotation;
					if (range.min() >= range.max())
						throwInvalidClass(clazz, "PluginMemberArgument");

					return new NetRange(range.min(), range.max());
				}
			}
		}

		throwInvalidClass(clazz, "PluginMemberArgument");
		return null; // inaccessible
	}

	public Class<?> getPluginClass() {
		return pluginClass;
	}

	public String getType() {
		return type;
	}

	public String getDisplayType() {
		return displayType;
	}

	public boolean isUi() {
		return ui;
	}

	public byte[] getImage() {
		return image;
	}

	public Class<?> getConfigurationInterface() {
		return configurationInterface;
	}

	public Collection<Method> getInitMethods() {
		return initMethods;
	}

	public Collection<Method> getDestroyMethods() {
		return destroyMethods;
	}

	public Collection<MemberMetadata> getMembers() {
		return members;
	}
}
