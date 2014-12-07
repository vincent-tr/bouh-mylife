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

/**
 * gpio pwm
 * 
 * @author pumbawoman
 * 
 */
public class GPIORGBComponent extends Component {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(GPIORGBComponent.class
			.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1197858893560489093L;
	
	/**
	 * accès gpio
	 */
	private PwmAccess accessR;
	
	/**
	 * accès gpio
	 */
	private PwmAccess accessG;
	
	/**
	 * accès gpio
	 */
	private PwmAccess accessB;
	
	/**
	 * Valeur
	 */
	int valueR = -1;
	
	/**
	 * Valeur
	 */
	int valueG = -1;
	
	/**
	 * Valeur
	 */
	int valueB = -1;
	
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
			int pinR = Integer.parseInt(getServletConfig().getInitParameter("pinR"));
			int pinG = Integer.parseInt(getServletConfig().getInitParameter("pinG"));
			int pinB = Integer.parseInt(getServletConfig().getInitParameter("pinB"));
			accessR = (PwmAccess)PwmAccessFactory.getInstance().openAccess(pinR);
			accessG = (PwmAccess)PwmAccessFactory.getInstance().openAccess(pinG);
			accessB = (PwmAccess)PwmAccessFactory.getInstance().openAccess(pinB);
			accessR.setPeriod(PERIOD);
			accessG.setPeriod(PERIOD);
			accessB.setPeriod(PERIOD);
		} catch (Exception e) {
			if(accessR != null)
				PwmAccessFactory.getInstance().closeAccess(accessR);
			if(accessG != null)
				PwmAccessFactory.getInstance().closeAccess(accessG);
			if(accessB != null)
				PwmAccessFactory.getInstance().closeAccess(accessB);

			throw new ServletException(e);
		}

		NetRange colorType = new NetRange(0, VALUE_MAX);
		NetAttribute attr0 = new NetAttribute(0, "r", colorType);
		NetAttribute attr1 = new NetAttribute(1, "g", colorType);
		NetAttribute attr2 = new NetAttribute(2, "b", colorType);
		NetAction action3 = new NetAction(3, "setR", colorType);
		NetAction action4 = new NetAction(4, "setG", colorType);
		NetAction action5 = new NetAction(5, "setB", colorType);
		NetAction action6 = new NetAction(6, "setColor", colorType, colorType,
				colorType);
		NetClass netClass = new NetClass(attr0, attr1, attr2, action3, action4,
				action5, action6);

		NetObject obj = new NetObject(getServletName(), netClass);
		
		// comportement
		obj.setActionExecutor("setR", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				int value = (Integer) arguments[0];
				try {
					setValue(value, valueG, valueB);
				} catch (Exception e) {
					log.log(Level.SEVERE, "error setR", e);
				}
			}
		});
		obj.setActionExecutor("setG", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				int value = (Integer) arguments[0];
				try {
					setValue(valueR, value, valueB);
				} catch (Exception e) {
					log.log(Level.SEVERE, "error setG", e);
				}
			}
		});
		obj.setActionExecutor("setB", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				int value = (Integer) arguments[0];
				try {
					setValue(valueR, valueG, value);
				} catch (Exception e) {
					log.log(Level.SEVERE, "error setB", e);
				}
			}
		});
		obj.setActionExecutor("setColor", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				int valueRloc = (Integer) arguments[0];
				int valueGloc = (Integer) arguments[1];
				int valueBloc = (Integer) arguments[2];
				try {
					setValue(valueRloc, valueGloc, valueBloc);
				} catch (Exception e) {
					log.log(Level.SEVERE, "error setColor", e);
				}
			}
		});

		// init
		setValue(0, 0, 0, obj);

		registerObject(obj, NetRepository.CHANNEL_HARDWARE);
	}

	/**
	 * Définition des valeurs
	 * @param r
	 * @param g
	 * @param b
	 * @param obj
	 */
	private void setValue(int r, int g, int b, NetObject obj) {
		if(r != valueR) {
			valueR = r;
			accessR.setPulse(PERIOD * valueR / VALUE_MAX );
			obj.setAttributeValue("r", valueR);
		}
		if(g != valueG) {
			valueG = g;
			accessG.setPulse(PERIOD * valueG / VALUE_MAX );
			obj.setAttributeValue("g", valueG);
		}
		if(b != valueB) {
			valueB = b;
			accessB.setPulse(PERIOD * valueB / VALUE_MAX );
			obj.setAttributeValue("b", valueB);
		}
	}
	
	/**
	 * Définition des valeurs
	 * @param r
	 * @param g
	 * @param b
	 */
	private void setValue(int r, int g, int b) {
		NetObject obj = getObject();
		setValue(r, g, b, obj);
	}
	
	@Override
	public void destroy() {
		
		if(accessR != null)
			PwmAccessFactory.getInstance().closeAccess(accessR);
		if(accessG != null)
			PwmAccessFactory.getInstance().closeAccess(accessG);
		if(accessB != null)
			PwmAccessFactory.getInstance().closeAccess(accessB);
		
		super.destroy();
	}

}
