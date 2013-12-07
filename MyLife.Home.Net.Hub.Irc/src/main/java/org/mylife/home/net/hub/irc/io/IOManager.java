package org.mylife.home.net.hub.irc.io;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gestion des IOElement
 * 
 * @author pumbawoman
 * 
 */
public class IOManager {

	private final Set<IOElement> elements = new HashSet<IOElement>();
	private final Selector selector;

	/**
	 * Initialisation
	 * 
	 * @throws IOException
	 */
	public IOManager() throws IOException {
		selector = Selector.open();
	}

	/**
	 * Fermeture
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		selector.close();
	}

	/**
	 * Ajout d'un élément
	 * 
	 * @param element
	 * @throws IOException
	 */
	public void addElement(IOElement element) throws IOException {
		element.open(selector);
		elements.add(element);
	}

	/**
	 * Suppression d'un élément
	 * 
	 * @param element
	 * @throws IOException
	 */
	public void removeElement(IOElement element) throws IOException {
		elements.remove(element);
		element.close();
	}

	/**
	 * Select
	 * 
	 * @param timeout
	 * @throws IOException
	 */
	public void select(long timeout) throws IOException {
		if (selector.select(timeout) == 0)
			return;

		// copie car checkSelect déclenche tout, y compris dse ajouts ou
		// suppression d'éléments
		List<IOElement> copy = new ArrayList<IOElement>(elements);
		for (IOElement element : copy) {
			if (!element.getSelectionKey().isValid())
				continue; // l'élément a été fermé entre temps
			element.checkSelect();
		}

	}

	/**
	 * Wakeup
	 */
	public void wakeup() {
		selector.wakeup();
	}

	/**
	 * Obtention d'une vue des éléments
	 * 
	 * @return
	 */
	public Set<IOElement> getElements() {
		return Collections.unmodifiableSet(elements);
	}
}
