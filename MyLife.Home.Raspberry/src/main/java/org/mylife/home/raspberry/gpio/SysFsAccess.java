package org.mylife.home.raspberry.gpio;

/**
 * Accès à un pin à travers sysfs
 * @author pumbawoman
 *
 */
public abstract class SysFsAccess {

	/**
	 * Pin représenté
	 */
	private final int pin;
	
	/**
	 * Chemin de base d'accès au pin
	 */
	private final String basePath;
	
	/**
	 * Constructeur avec initialisation des données
	 * @param pin
	 */
	protected SysFsAccess(int pin, String basePath) {
		this.pin = pin;
		this.basePath = basePath;
	}
	
	/**
	 * Pin représenté
	 * @return
	 */
	public int getPin() {
		return pin;
	}
	
	/**
	 * Chemin de base d'accès au pin
	 * @return
	 */
	protected String getBasePath() {
		return basePath;
	}
}
