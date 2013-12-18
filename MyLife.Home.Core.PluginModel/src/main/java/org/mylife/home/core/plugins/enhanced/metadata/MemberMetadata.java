package org.mylife.home.core.plugins.enhanced.metadata;

import java.lang.reflect.Method;


/**
 * Membre
 * 
 * @author pumbawoman
 * 
 */
public abstract class MemberMetadata {
	private final Method method;
	private final int index;
	private final String name;
	private final String displayName;

	public MemberMetadata(Method method, int index, String name, String displayName) {
		this.method = method;
		this.index = index;
		this.name = name;
		this.displayName = displayName;
	}

	public Method getMethod() {
		return method;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}
}
