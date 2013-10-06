package org.mylife.home.webcomponents;

import java.util.logging.Logger;

import org.mylife.home.raspberry.gpio.GpioAccess;

/**
 * Composant en entrée simple
 * @author pumbawoman
 *
 */
public class GPIOInputComponent extends Component {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(GPIOInputComponent.class
			.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 4575207442542259639L;

	/**
	 * accès gpio
	 */
	private GpioAccess access;
}
