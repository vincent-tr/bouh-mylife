package org.mylife.home.net.hub.irc.io;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Base d'IO
 * 
 * @author pumbawoman
 * 
 */
public abstract class IOElement {

	private SelectionKey selectionKey;

	public final SelectionKey getSelectionKey() {
		return selectionKey;
	}

	public final void open(Selector selector) throws IOException {
		selectionKey = openImpl(selector);
	}

	/**
	 * Enregistrement sur le selector
	 * 
	 * @param selector
	 * @return
	 */
	protected abstract SelectionKey openImpl(Selector selector)
			throws IOException;

	public final void checkSelect() throws IOException {

		Selector selector = selectionKey.selector();

		if (!selector.selectedKeys().contains(selectionKey))
			return;
		if (selectionKey.readyOps() <= 0)
			return;

		selectImpl();

		// On consifère que les opérations ont été traitées donc on l'enlève du
		// selector
		selector.selectedKeys().remove(selectionKey);
	}

	/**
	 * Déclenché quand le sélectionné par le sélector
	 */
	protected abstract void selectImpl() throws IOException;

	/**
	 * Fermeture de l'élément
	 */
	public final void close() throws IOException {
		selectionKey.cancel();
		closeImpl();
	}

	protected void closeImpl() throws IOException {

	}
}
