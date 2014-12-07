package org.mylife.home.raspberry.gpio;

import java.util.HashSet;
import java.util.Set;

/**
 * Accès GPIO
 * 
 * @author pumbawoman
 * 
 */
public class GpioAccess extends SysFsAccess {

	public static final boolean INPUT = false;
	public static final boolean OUTPUT = true;

	private Set<InputValueChangedListener> listeners = new HashSet<InputValueChangedListener>();
	private boolean direction = INPUT;
	
	public GpioAccess(int pin, SysFsAccessFactory creator) {
		super(pin, creator);
	}

	/**
	 * Ajout d'un listener sur entrée changée
	 * @param listener
	 */
	public void addListener(InputValueChangedListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Ajout d'un listener sur entrée changée
	 * @param listener
	 * @return
	 */
	public boolean removeListener(InputValueChangedListener listener) {
		return listeners.remove(listener);
	}
	
	/**
	 * Déclenchement des listeners
	 */
	/* internal */ void fireListeners() {
		boolean newValue = getValue();
		for(InputValueChangedListener listener : listeners)
			listener.onChanged(this, newValue);
	}
	
	/* internal */ void init() {
		// initialement en entrée
		direction = INPUT;
		writeFile("direction","in");
		GpioInputMonitor.getInstance().startMonitoring(this);
	}
	
	/* internal */ void terminate() {
		// on le remet en entrée mais sans monitoring
		if(direction == OUTPUT)
			writeFile("direction","in");
		else
			GpioInputMonitor.getInstance().stopMonitoring(this);
	}
	
	/**
	 * Direction
	 * 
	 * @return
	 */
	public boolean getDirection() {
		return direction;
	}

	/**
	 * Direction
	 * 
	 * @param value
	 */
	public void setDirection(boolean value) {
		if(value == direction)
			return;
		
		direction = value;
		writeFile("direction", direction == OUTPUT ? "out" : "in");
		
		if(direction == OUTPUT)
			GpioInputMonitor.getInstance().stopMonitoring(this);
		else
			GpioInputMonitor.getInstance().startMonitoring(this);
	}
	
	/**
	 * Valeur
	 * @return
	 */
	public boolean getValue() {
		String content = readFile("value");
		return "1".equals(content);
	}
	
	/**
	 * Valeur
	 * @param value
	 */
	public void setValue(boolean value) {
		writeFile("value", value ? "1" : "0");
	}
}
