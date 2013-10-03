package org.mylife.home.raspberry.gpio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Accès à un pin à travers sysfs
 * 
 * @author pumbawoman
 * 
 */
public abstract class SysFsAccess {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(SysFsAccess.class
			.getName());

	/**
	 * Pin représenté
	 */
	private final int pin;

	/**
	 * Créateur
	 */
	private final SysFsAccessFactory creator;

	/**
	 * Chemin de base d'accès au pin
	 */
	private final String basePath;

	/**
	 * Constructeur avec initialisation des données
	 * 
	 * @param pin
	 * @param creator
	 */
	protected SysFsAccess(int pin, SysFsAccessFactory creator) {
		this.pin = pin;
		this.creator = creator;
		basePath = String.format("%s%s%d/", creator.getClassPath(),
				creator.getDevicePrefix(), pin);
	}

	/**
	 * Pin représenté
	 * 
	 * @return
	 */
	public int getPin() {
		return pin;
	}

	/**
	 * Créateur
	 */
	protected SysFsAccessFactory getCreator() {
		return creator;
	}

	/**
	 * Chemin de base d'accès au pin
	 * 
	 * @return
	 */
	protected String getBasePath() {
		return basePath;
	}

	/**
	 * Ecriture dans un fichier
	 * 
	 * @param name
	 * @return
	 */
	protected String readFile(String name) {
		try {
			String path = getBasePath() + name;
			log.log(Level.INFO, String.format("reading file '%s'", path));
			InputStream is = new FileInputStream(path);
			try {
				byte[] buffer = new byte[128]; // tres largement suffisant
				int count = is.read(buffer);
				return new String(buffer, 0, count);
			} finally {
				is.close();
			}
		} catch (IOException e) {
			throw new SysFsAccessException(e);
		}
	}

	/**
	 * Lecture d'un fichier
	 * 
	 * @param name
	 * @param value
	 */
	protected void writeFile(String name, String value) {
		try {
			String path = getBasePath() + name;
			log.log(Level.INFO, String.format("writing file '%s' => '%s'", path, value));
			OutputStream os = new FileOutputStream(path, false);
			try {
				os.write(value.getBytes());
			} finally {
				os.close();
			}
		} catch (IOException e) {
			throw new SysFsAccessException(e);
		}
	}
}
