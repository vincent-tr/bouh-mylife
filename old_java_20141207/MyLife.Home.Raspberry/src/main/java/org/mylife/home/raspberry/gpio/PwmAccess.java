package org.mylife.home.raspberry.gpio;

/**
 * Accès PWM
 * @author pumbawoman
 *
 */
public class PwmAccess extends SysFsAccess {

	public PwmAccess(int pin, SysFsAccessFactory creator) {
		super(pin, creator);
	}

	/**
	 * Périod (usec)
	 * @return
	 */
	public int getPeriod() {
		String content = readFile("period");
		// format : xxx usec
		return Integer.parseInt(content.split(" ")[0]);
	}
	
	/**
	 * Période (usec)
	 * @param value
	 */
	public void setPeriod(int value) {
		writeFile("period", Integer.toString(value));
	}
	
	/**
	 * Pulse (usec)
	 * @return
	 */
	public int getPulse() {
		String content = readFile("pulse");
		// format : xxx usec
		return Integer.parseInt(content.split(" ")[0]);
	}
	
	/**
	 * Pulse (usec)
	 * @param value
	 */
	public void setPulse(int value) {
		writeFile("pulse", Integer.toString(value));
	}

	/**
	 * Pulse (usec)
	 * @return
	 */
	public int getPulses() {
		String content = readFile("pulses");
		// format : xxx usec
		return Integer.parseInt(content.split(" ")[0]);
	}
	
	/**
	 * Pulse (usec)
	 * @param value
	 */
	public void setPulses(int value) {
		writeFile("pulses", Integer.toString(value));
	}
	
}
