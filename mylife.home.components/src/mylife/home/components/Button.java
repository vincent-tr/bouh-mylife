package mylife.home.components;

import mylife.home.hw.api.InputDevice;
import mylife.home.hw.api.InputDeviceListener;

/**
 * Implémentation de base d'un bouton
 * @author pumbawoman
 *
 */
public class Button extends Component implements InputDeviceListener {

	private InputDevice device;
	
	@Override
	public void init() {
		super.init();
		
		try {
			device = (InputDevice)openPinFromConfiguration(null);
			device.addListener(this);
			stateChanged(device, device.getValue());
			
		} catch (Exception e) {
			throw new RuntimeException("Initialization error", e);
		}
	}

	@Override
	public void close() throws Exception {
		super.close();
		if(device != null) {
			device.close();
		}
	}

	@Override
	public void stateChanged(InputDevice device, boolean state) {
		getNet().setStatus(state ? "on" : "off");
	}

}
