package org.mylife.home.net.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Enum�ration
 * 
 * @author pumbawoman
 * 
 */
public class NetEnum extends NetType {

	private final Collection<String> values;

	public NetEnum(Iterable<String> items) {
		ArrayList<String> list = new ArrayList<String>();
		for(String item : items)
			list.add(item);
		values = Collections.unmodifiableCollection(list);
	}
	
	public NetEnum(String... items) {
		this(Arrays.asList(items));
	}

	public Collection<String> getValues() {
		return values;
	}

}
