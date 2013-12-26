package org.mylife.home.ui.structure;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestion des fenêtres
 * 
 * @author pumbawoman
 * 
 */
public final class Windows {

	private Windows() {
	}
	
	private static final Map<String, Window> list = Collections.synchronizedMap(new HashMap<String, Window>());
	
	public static Collection<Window> list() {
		return list.values();
	}
	
	public static Window getWindow(String id) {
		return list.get(id);
	}
}
