package org.mylife.home.core.plugins.enhanced;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

public final class Helpers {

	private Helpers() {

	}

	public static Object valueOfEnum(Class<?> clazz, String value) {
		if (StringUtils.isEmpty(value))
			return null;

		Object[] values = clazz.getEnumConstants();
		for (Object item : values) {
			if (item.toString().equalsIgnoreCase(value))
				return item;
		}
		return null;
	}

	public static Collection<String> namesOfEnum(Class<?> clazz) {
		Collection<String> values = new ArrayList<String>();
		for (Object value : clazz.getEnumConstants()) {
			values.add(value.toString());
		}
		return values;

	}

}
