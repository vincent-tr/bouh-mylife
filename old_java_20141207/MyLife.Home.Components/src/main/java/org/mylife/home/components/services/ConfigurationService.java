package org.mylife.home.components.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.mylife.home.common.services.Service;
import org.mylife.home.components.data.DataConfiguration;
import org.mylife.home.components.data.DataConfigurationAccess;
import org.mylife.home.components.providers.ComponentConfiguration;
import org.mylife.home.components.providers.ComponentFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ConfigurationService implements Service {

	/**
	 * Logger
	 */
	private final static Logger log = Logger
			.getLogger(ConfigurationService.class.getName());

	/* internal */ConfigurationService() {

	}

	@Override
	public void terminate() {

	}

	private Map<String, String> types;

	public Map<String, String> listTypes() {
		if (types == null) {
			Map<String, String> map = new HashMap<String, String>();
			Collection<ComponentFactory> factories = ServiceAccess
					.getInstance().getComponentService().getFactories();
			for (ComponentFactory factory : factories) {
				map.put(factory.getType(), factory.getDisplayType());
			}
			types = Collections.unmodifiableMap(map);
		}
		return types;
	}

	/**
	 * Obtention des configurations actives
	 * 
	 * @return
	 */
	public List<ComponentConfiguration> listActives() {
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			List<ComponentConfiguration> list = new ArrayList<ComponentConfiguration>();
			for (DataConfiguration item : access.getConfigurationsActives()) {
				list.add(map(item));
			}
			return list;
		} finally {
			access.close();
		}
	}

	/**
	 * Obtention de toutes les configurations
	 * 
	 * @return
	 */
	public List<ComponentConfiguration> list() {
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			List<ComponentConfiguration> list = new ArrayList<ComponentConfiguration>();
			for (DataConfiguration item : access.getConfigurationsAll()) {
				list.add(map(item));
			}
			return list;
		} finally {
			access.close();
		}
	}

	/**
	 * Obtention d'une configuration
	 * 
	 * @param id
	 * @return
	 */
	public ComponentConfiguration get(int id) {
		Validate.isTrue(id > 0);
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			DataConfiguration item = access.getConfigurationByKey(id);
			if (item == null)
				return null;
			return map(item);
		} finally {
			access.close();
		}

	}

	/**
	 * Suppression d'une configuration
	 * 
	 * @param id
	 */
	public void delete(int id) {
		Validate.isTrue(id > 0);
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			DataConfiguration item = new DataConfiguration();
			item.setId(id);
			access.deleteConfiguration(item);
		} finally {
			access.close();
		}
	}

	/**
	 * Création d'une configuration
	 * 
	 * @param config
	 */
	public void create(ComponentConfiguration config) {
		Validate.notNull(config);
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			DataConfiguration item = map(config);
			access.createConfiguration(item);
		} finally {
			access.close();
		}
	}

	/**
	 * MAJ d'une configuration
	 * 
	 * @param config
	 */
	public void update(ComponentConfiguration config) {
		Validate.notNull(config);
		Validate.isTrue(config.getDataId() > 0);
		DataConfigurationAccess access = new DataConfigurationAccess();
		try {
			DataConfiguration item = map(config);
			access.updateConfiguration(item);
		} finally {
			access.close();
		}
	}

	private ComponentConfiguration map(DataConfiguration source) {
		ComponentConfiguration dest = new ComponentConfiguration();
		dest.setDataId(source.getId());
		dest.setComponentId(source.getComponentId());
		dest.setType(source.getType());
		dest.setActive(source.isActive());

		byte[] sourceParameters = source.getParameters();
		if (sourceParameters != null && sourceParameters.length > 0) {
			InputStreamReader reader = new InputStreamReader(
					new ByteArrayInputStream(sourceParameters));

			Gson gson = new Gson();
			Type typeOfHashMap = new TypeToken<Map<String, String>>() {
			}.getType();
			Map<String, String> destParameters = gson.fromJson(reader,
					typeOfHashMap);
			for (Map.Entry<String, String> item : destParameters.entrySet()) {
				dest.getParameters().put(item.getKey(), item.getValue());
			}
		}
		return dest;
	}

	private DataConfiguration map(ComponentConfiguration source) {
		DataConfiguration dest = new DataConfiguration();
		dest.setId(source.getDataId());
		dest.setComponentId(source.getComponentId());
		dest.setType(source.getType());
		dest.setActive(source.isActive());

		Gson gson = new Gson();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(stream);
		gson.toJson(source.getParameters(), writer);
		try {
			writer.flush();
		} catch (IOException e) {
			log.log(Level.SEVERE, "error flushing", e);
		}
		dest.setParameters(stream.toByteArray());

		return dest;
	}
}
