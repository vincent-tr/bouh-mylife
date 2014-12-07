package org.mylife.home.components.providers.impl.test;

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
import org.mylife.home.net.structure.NetRange;

/**
 * Composant de test
 * 
 * @author pumbawoman
 * 
 */
public class TestComponent implements Component {

	/**
	 * Fabrique
	 * @author pumbawoman
	 *
	 */
	public static class Factory extends BaseComponentFactory {

		public Factory() {
			super(TestComponent.class);
		}
	}

	@Override
	public void init(ComponentContext context) throws Exception {
		
		NetEnum onOffType = new NetEnum("off", "on");
		NetRange volumeType = new NetRange(0, 100);
		NetClass netClass = new NetClass(
				new NetAttribute(0, "onOff", onOffType),
				new NetAttribute(1, "volume", volumeType),
				new NetAction(2, "setOn"),
				new NetAction(3, "setOff"),
				new NetAction(4, "setVolume", volumeType),
				new NetAction(5, "setOnOff", onOffType));
		
		NetObject obj = new NetObject(context.componentId(), netClass);

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
		obj.setActionExecutor("setOnOff", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				String value = (String)arguments[0];
				obj.setAttributeValue("onOff", value);
			}
		});
		
		context.registerObject(obj, NetRepository.CHANNEL_HARDWARE);
	}

	@Override
	public void destroy() {
	}

}
