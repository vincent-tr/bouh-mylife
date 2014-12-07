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
import org.mylife.home.net.structure.NetEnum;
import org.mylife.home.raspberry.gpio.GpioAccess;
import org.mylife.home.raspberry.gpio.GpioAccessFactory;

/**
 * Composant en sortie simple
 * 
 * @author pumbawoman
 * 
 */
public class GPIOOutputComponent implements Component {

	/**
	 * Fabrique
	 * 
	 * @author pumbawoman
	 * 
	 */
	public static class Factory extends BaseComponentFactory {

		public Factory() {
			super(GPIOOutputComponent.class, "pin");
		}
	}

	/**
	 * Logger
	 */
	private final static Logger log = Logger
			.getLogger(GPIOOutputComponent.class.getName());

	/**
	 * Contexte
	 */
	private ComponentContext context;

	/**
	 * accès gpio
	 */
	private GpioAccess access;

	/**
	 * Valeur
	 */
	boolean value;

	@Override
	public void init(ComponentContext context) throws Exception {
		this.context = context;

		try {
			int pin = Integer.parseInt(context.parameters().get("pin"));
			access = (GpioAccess) GpioAccessFactory.getInstance().openAccess(
					pin);
			access.setDirection(GpioAccess.OUTPUT);
			access.setValue(false);
		} finally {
			if (access != null)
				GpioAccessFactory.getInstance().closeAccess(access);
		}

		NetEnum valueType = new NetEnum("off", "on");
		NetAttribute attr0 = new NetAttribute(0, "value", valueType);
		NetAction action1 = new NetAction(1, "setValue", valueType);
		NetClass netClass = new NetClass(attr0, action1);

		NetObject obj = new NetObject(context.componentId(), netClass);

		// comportement
		obj.setActionExecutor("setValue", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				boolean value = "on".equals((String) arguments[0]);
				try {
					setValue(value);
				} catch (Exception e) {
					log.log(Level.SEVERE, "error setValue", e);
				}
			}
		});

		// init
		this.value = true; // pour init
		setValue(false, obj);

		context.registerObject(obj, NetRepository.CHANNEL_HARDWARE);
	}

	/**
	 * Définition des valeurs
	 * 
	 * @param value
	 * @param obj
	 */
	private void setValue(boolean value, NetObject obj) {
		if (this.value != value) {
			this.value = value;
			access.setValue(value);
			obj.setAttributeValue("value", this.value ? "on" : "off");
		}
	}

	/**
	 * Définition des valeurs
	 * 
	 * @param value
	 */
	private void setValue(boolean value) {
		NetObject obj = context.getObject();
		setValue(value, obj);
	}

	@Override
	public void destroy() {

		if (access != null)
			GpioAccessFactory.getInstance().closeAccess(access);
	}
}
