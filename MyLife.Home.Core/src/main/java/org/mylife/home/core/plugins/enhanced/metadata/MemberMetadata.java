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

	public MemberMetadata(Method method, int index, String name) {
		this.method = method;
		this.index = index;
		this.name = name;
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
}
