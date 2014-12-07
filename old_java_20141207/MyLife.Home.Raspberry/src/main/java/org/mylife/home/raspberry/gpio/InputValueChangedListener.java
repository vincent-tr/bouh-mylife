package org.mylife.home.raspberry.gpio;

/**
 * Interface déclenchée sur valeur changée en entrée (seulement en direction INPUT)
 * @author pumbawoman
 *
 */
public interface InputValueChangedListener {
	
	/**
	 * Appelé sur changement
	 * @param access
	 * @param newValue
	 */
	void onChanged(GpioAccess access, boolean newValue);
	
}
