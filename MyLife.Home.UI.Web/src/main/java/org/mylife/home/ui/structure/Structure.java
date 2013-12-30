package org.mylife.home.ui.structure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.mylife.home.net.exchange.ui.XmlUiContainer;
import org.mylife.home.net.exchange.ui.XmlUiImage;
import org.mylife.home.net.exchange.ui.XmlUiWindow;

/**
 * Gestion des fenêtres
 * 
 * @author pumbawoman
 * 
 */
public final class Structure {

	private Structure() {
	}

	/**
	 * Mise à jour réservé pour DispatcherService
	 * 
	 * @param container
	 */
	public synchronized static void update(XmlUiContainer container) {

		reset();

		if (container == null)
			return;

		try {
			// On lit en 1er les images
			if (container.images != null) {
				for (XmlUiImage xmlImage : container.images) {
					images.put(xmlImage.id, xmlImage.content);
				}
			}

			// Puis les fenêtres
			if (container.windows != null) {
				for (XmlUiWindow xmlWindow : container.windows) {
					Window window = new Window(xmlWindow);
					windows.put(window.getId(), window);
				}
			}

			// Fenêtre par défaut
			if (!StringUtils.isEmpty(container.defaultWindowId)) {
				defaultWindow = windows.get(container.defaultWindowId);
			}
		} catch (Exception e) {
			reset();
			throw e;
		}
	}

	private static void reset() {
		images.clear();
		windows.clear();
	}

	private static final Map<String, Window> windows = Collections
			.synchronizedMap(new HashMap<String, Window>());
	private static final Map<String, byte[]> images = Collections
			.synchronizedMap(new HashMap<String, byte[]>());
	private static Window defaultWindow;

	public static Collection<Window> getWindows() {
		return Collections.unmodifiableCollection(windows.values());
	}

	public static Window getDefaultWindow() {
		return defaultWindow;
	}

	public static Window getWindow(String id) {
		return windows.get(id);
	}

	public static Set<String> getImageIds() {
		return Collections.unmodifiableSet(images.keySet());
	}

	public static InputStream getImage(String id) {
		byte[] value = images.get(id);
		if (value == null)
			return null;
		return new ByteArrayInputStream(value);
	}
}
