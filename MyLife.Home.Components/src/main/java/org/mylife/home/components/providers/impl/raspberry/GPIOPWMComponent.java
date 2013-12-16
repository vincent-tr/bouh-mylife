package org.mylife.home.components.providers.impl.raspberry;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.components.providers.Component;
import org.mylife.home.components.providers.ComponentContext;
import org.mylife.home.components.providers.impl.BaseComponentFactory;
import org.mylife.home.net.ActionExecutor;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.NetRepository;
import org.mylife.home.net.structure.NetAction;
import org.mylife.home.net.structure.NetAttribute;
import org.mylife.home.net.structure.NetClass;
import org.mylife.home.net.structure.NetRange;
import org.mylife.home.raspberry.gpio.PwmAccess;
import org.mylife.home.raspberry.gpio.PwmAccessFactory;

public class GPIOPWMComponent implements Component {

	/**
	 * Fabrique
	 * 
	 * @author pumbawoman
	 * 
	 */
	public static class Factory extends BaseComponentFactory {

		public Factory() {
			super(GPIOPWMComponent.class, "pin");
		}
	}

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(GPIOPWMComponent.class
			.getName());

	/**
	 * Contexte
	 */
	private ComponentContext context;

	/**
	 * accès gpio
	 */
	private PwmAccess access;

	/**
	 * Valeur
	 */
	int value = -1;

	/**
	 * Périod
	 */
	private static final int PERIOD = 10000;

	/**
	 * Valeur max
	 */
	private static final int VALUE_MAX = 255;

	@Override
	public void init(ComponentContext context) throws Exception {
		this.context = context;

		try {
			int pin = Integer.parseInt(context.parameters().get("pin"));
			access = (PwmAccess) PwmAccessFactory.getInstance().openAccess(pin);
			access.setPeriod(PERIOD);
		} finally {
			if (access != null)
				PwmAccessFactory.getInstance().closeAccess(access);
		}

		NetRange colorType = new NetRange(0, VALUE_MAX);
		NetAttribute attr0 = new NetAttribute(0, "value", colorType);
		NetAction action1 = new NetAction(1, "setValue", colorType);
		NetClass netClass = new NetClass(attr0, action1);

		NetObject obj = new NetObject(context.componentId(), netClass);

		// comportement
		obj.setActionExecutor("setValue", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				int value = (Integer) arguments[0];
				try {
					setValue(value);
				} catch (Exception e) {
					log.log(Level.SEVERE, "error setValue", e);
				}
			}
		});

		// init
		setValue(0, obj);

		context.registerObject(obj, NetRepository.CHANNEL_HARDWARE);
	}

	/**
	 * Définition des valeurs
	 * 
	 * @param value
	 * @param obj
	 */
	private void setValue(int value, NetObject obj) {
		if (this.value != value) {
			this.value = value;
			access.setPulse(PERIOD * this.value / VALUE_MAX);
			obj.setAttributeValue("value", this.value);
		}
	}

	/**
	 * Définition des valeurs
	 * 
	 * @param value
	 */
	private void setValue(int value) {
		NetObject obj = context.getObject();
		setValue(value, obj);
	}

	@Override
	public void destroy() {

		if (access != null)
			PwmAccessFactory.getInstance().closeAccess(access);
	}
}
