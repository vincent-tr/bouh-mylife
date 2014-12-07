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
import org.mylife.home.net.structure.NetEnum;
import org.mylife.home.raspberry.gpio.GpioAccess;
import org.mylife.home.raspberry.gpio.GpioAccessFactory;

/**
 * Composant en sortie simple
 * @author pumbawoman
 *
 */
public class GPIOOutputComponent extends Component {
	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(GPIOOutputComponent.class
			.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 415729852996184659L;

	/**
	 * accès gpio
	 */
	private GpioAccess access;

	/**
	 * Valeur
	 */
	boolean value;

	@Override
	protected void create() throws ServletException {

		try {
			int pin = Integer.parseInt(getServletConfig().getInitParameter(
					"pin"));
			access = (GpioAccess) GpioAccessFactory.getInstance().openAccess(pin);
			access.setDirection(GpioAccess.OUTPUT);
			access.setValue(false);
		} catch (Exception e) {
			if (access != null)
				GpioAccessFactory.getInstance().closeAccess(access);

			throw new ServletException(e);
		}

		NetEnum valueType = new NetEnum("off", "on");
		NetAttribute attr0 = new NetAttribute(0, "value", valueType);
		NetAction action1 = new NetAction(1, "setValue", valueType);
		NetClass netClass = new NetClass(attr0, action1);

		NetObject obj = new NetObject(getServletName(), netClass);

		// comportement
		obj.setActionExecutor("setValue", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				boolean value = "on".equals((String)arguments[0]);
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

		registerObject(obj, NetRepository.CHANNEL_HARDWARE);
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
		NetObject obj = getObject();
		setValue(value, obj);
	}

	@Override
	public void destroy() {

		if (access != null)
			GpioAccessFactory.getInstance().closeAccess(access);

		super.destroy();
	}
}
