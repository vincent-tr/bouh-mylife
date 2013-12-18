package org.mylife.home.core.plugins.enhanced;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Attribut d'un Plugin avancé
 * 
 * @author pumbawoman
 * 
 */
public class Attribute<T> {

	/**
	 * Changement de valeur de l'attribut
	 * 
	 * @author pumbawoman
	 * 
	 */
	public static interface ChangeListener {
		public void attributeChanged(Attribute<?> owner, Object value);
	}

	/**
	 * Gestion des listeners
	 */
	private final Collection<ChangeListener> listeners = new ArrayList<ChangeListener>();

	/**
	 * Ajout d'un listener
	 * 
	 * @param listener
	 */
	public synchronized void addListener(ChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Suppression d'un listener
	 * 
	 * @param listener
	 */
	public synchronized void removeListener(ChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Valeur
	 */
	private T value;

	/**
	 * Constructeur par défaut
	 */
	public Attribute() {
	}

	/**
	 * Constructeur avec une valeur initiale
	 * @param initialValue
	 */
	public Attribute(T initialValue) {
		this.value = initialValue;
	}
	
	/**
	 * Définition de la valeur
	 * 
	 * @param value
	 */
	public synchronized void setValue(T value) {
		this.value = value;
		for (ChangeListener listener : listeners)
			listener.attributeChanged(this, value);
	}

	/**
	 * Obtention de la valeur
	 * 
	 * @return
	 */
	public T getValue() {
		return value;
	}

}
