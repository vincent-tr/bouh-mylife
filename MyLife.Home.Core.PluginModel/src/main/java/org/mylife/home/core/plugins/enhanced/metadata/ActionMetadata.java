package org.mylife.home.core.plugins.enhanced.metadata;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import org.mylife.home.net.structure.NetType;

public class ActionMetadata extends MemberMetadata {

	private final Collection<NetType> netTypes;

	public ActionMetadata(Method method, int index, String name,
			String displayName, Collection<NetType> netTypes) {
		super(method, index, name, displayName);
		this.netTypes = Collections.unmodifiableCollection(netTypes);
	}

	public Collection<NetType> getNetTypes() {
		return netTypes;
	}
}
