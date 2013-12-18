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
	private final String toStringCache;

	public NetEnum(Iterable<String> items) {
		ArrayList<String> list = new ArrayList<String>();
		for (String item : items)
			list.add(item);
		values = Collections.unmodifiableCollection(list);
		
		StringBuffer builder = new StringBuffer();
		for(String value : values) {
			if(builder.length() > 0)
				builder.append(',');
			builder.append(value);
		}
		toStringCache = "Enum[" + builder + "]";
	}

	public NetEnum(String... items) {
		this(Arrays.asList(items));
	}

	public Collection<String> getValues() {
		return values;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NetRange))
			return false;
		NetEnum other = (NetEnum) obj;

		if(other.values.size() != values.size())
			return false;
		
		// on considère que l'ordre ne compte pas
		String[] thisValues = values.toArray(new String[values.size()]);
		String[] otherValues = other.values.toArray(new String[other.values.size()]);
		
		Arrays.sort(thisValues);
		Arrays.sort(otherValues);
		
		for(int i=0; i<thisValues.length; i++) {
			if(!thisValues[i].equals(otherValues[i]))
				return false;
		}
		
		return true;
	}

	@Override
	public int hashCode() {
		return toStringCache.hashCode();
	}

	@Override
	public String toString() {
		return toStringCache;
	}
}
