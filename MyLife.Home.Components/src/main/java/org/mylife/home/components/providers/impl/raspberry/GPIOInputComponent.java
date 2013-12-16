package org.mylife.home.components.providers.impl.raspberry;

import java.util.logging.Logger;

import org.mylife.home.components.providers.Component;
import org.mylife.home.components.providers.ComponentContext;
import org.mylife.home.components.providers.impl.BaseComponentFactory;
import org.mylife.home.raspberry.gpio.GpioAccess;

/**
 * Composant en entrée simple
 * @author pumbawoman
 *
 */
public class GPIOInputComponent implements Component {

	/**
	 * Fabrique
	 * @author pumbawoman
	 *
	 */
	public static class Factory extends BaseComponentFactory {

		public Factory() {
			super(GPIOInputComponent.class);
		}
	}

	/**
	 * Contexte
	 */
	private ComponentContext context;

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(GPIOInputComponent.class
			.getName());

	/**
	 * accès gpio
	 */
	private GpioAccess access;

	@Override
	public void init(ComponentContext context) throws Exception {
		this.context = context;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
}
