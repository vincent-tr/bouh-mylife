package org.mylife.home.core.plugins.enhanced.metadata;

import java.lang.reflect.Method;

import org.mylife.home.net.structure.NetType;

public class AttributeMetadata extends MemberMetadata {

	private final NetType netType;

	public AttributeMetadata(Method method, int index, String name,
			NetType netType) {
		super(method, index, name);
		this.netType = netType;
	}

	public NetType getNetType() {
		return netType;
	}
}
