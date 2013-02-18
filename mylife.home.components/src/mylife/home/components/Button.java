package mylife.home.components;

import java.util.EnumSet;

import mylife.home.hw.api.DeviceAccessDeniedException;
import mylife.home.hw.api.InputDevice;
import mylife.home.hw.api.InputDeviceListener;
import mylife.home.hw.api.Options;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.metatype.Meta;

/**
 * Implémentation de base d'un bouton
 * @author pumbawoman
 *
 */
@aQute.bnd.annotation.component.Component(
		provide=Object.class, 
		configurationPolicy=ConfigurationPolicy.require,
		designateFactory=Button.ButtonConfiguration.class)
public class Button extends ComponentBase implements InputDeviceListener {

	@Meta.OCD(name="MyLife.Home Component Button")
	public interface ButtonConfiguration extends ComponentBase.ConfigurationBase {

		@Meta.AD(name="Component ID")
		String id();
		
		@Meta.AD(name="Component display")
		String display();
		
		@Meta.AD(name="pin ID")
		int pinId();
}
	
	@Override
	public void initialize() {
		super.initialize();
		
		ButtonConfiguration config = (ButtonConfiguration)getConfiguration();
		InputDevice device = null;
		try {			
			device = (InputDevice)openPin(config.pinId(), EnumSet.of(Options.DIRECTION_INPUT));
		} catch (DeviceAccessDeniedException e) {
			throw new RuntimeException(e);
		}
		device.addListener(this);
		stateChanged(device, device.getValue());
	}

	@Override
	public void stateChanged(InputDevice device, boolean state) {
		getNet().setStatus(state ? "on" : "off");
	}

	@Override
	protected Class<?> getConfigurationClass() {
		return ButtonConfiguration.class;
	}

}
