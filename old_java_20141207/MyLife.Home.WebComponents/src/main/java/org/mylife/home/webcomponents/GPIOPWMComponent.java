package org.mylife.home.webcomponents;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.mylife.home.net.ActionExecutor;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.NetRepository;
import org.mylife.home.net.structure.NetAction;
import org.mylife.home.net.structure.NetAttribute;
import org.mylife.home.net.structure.NetClass;
import org.mylife.home.net.structure.NetRange;
import org.mylife.home.raspberry.gpio.PwmAccess;
import org.mylife.home.raspberry.gpio.PwmAccessFactory;

public class GPIOPWMComponent extends Component {
	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(GPIOPWMComponent.class
			.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -6741182820879910037L;

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
	protected void create() throws ServletException {

		try {
			int pin = Integer.parseInt(getServletConfig().getInitParameter(
					"pin"));
			access = (PwmAccess) PwmAccessFactory.getInstance().openAccess(pin);
			access.setPeriod(PERIOD);
		} catch (Exception e) {
			if (access != null)
				PwmAccessFactory.getInstance().closeAccess(access);

			throw new ServletException(e);
		}

		NetRange colorType = new NetRange(0, VALUE_MAX);
		NetAttribute attr0 = new NetAttribute(0, "value", colorType);
		NetAction action1 = new NetAction(1, "setValue", colorType);
		NetClass netClass = new NetClass(attr0, action1);

		NetObject obj = new NetObject(getServletName(), netClass);

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

		registerObject(obj, NetRepository.CHANNEL_HARDWARE);
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
		NetObject obj = getObject();
		setValue(value, obj);
	}

	@Override
	public void destroy() {

		if (access != null)
			PwmAccessFactory.getInstance().closeAccess(access);

		super.destroy();
	}
}
