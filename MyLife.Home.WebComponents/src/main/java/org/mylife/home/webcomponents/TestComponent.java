package org.mylife.home.webcomponents;

import org.mylife.home.net.ActionExecutor;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.NetRepository;
import org.mylife.home.net.structure.NetAction;
import org.mylife.home.net.structure.NetAttribute;
import org.mylife.home.net.structure.NetClass;
import org.mylife.home.net.structure.NetEnum;
import org.mylife.home.net.structure.NetRange;

/**
 * Composant de test
 * @author pumbawoman
 *
 */
public class TestComponent extends Component {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7381777558729854013L;

	/**
	 * Création de l'objet
	 */
	@Override
	protected void create() {
		
		NetEnum onOffType = new NetEnum("off", "on");
		NetRange volumeType = new NetRange(0, 100);
		NetClass netClass = new NetClass(
				new NetAttribute(0, "onOff", onOffType),
				new NetAttribute(1, "volume", volumeType),
				new NetAction(2, "setOn"),
				new NetAction(3, "setOff"),
				new NetAction(4, "setVolume", volumeType));
		
		NetObject obj = new NetObject(getServletName(), netClass);

		// init
		obj.setAttributeValue("onOff", "off");
		obj.setAttributeValue("volume", Integer.valueOf(0));

		// comportement
		obj.setActionExecutor("setOn", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				obj.setAttributeValue("onOff", "on");
			}
		});
		obj.setActionExecutor("setOff", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				obj.setAttributeValue("onOff", "off");
			}
		});
		obj.setActionExecutor("setVolume", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				int value = ((Integer)arguments[0]).intValue();
				obj.setAttributeValue("volume", value);
			}
		});
		
		registerObject(obj, NetRepository.CHANNEL_HARDWARE);
	}
}
