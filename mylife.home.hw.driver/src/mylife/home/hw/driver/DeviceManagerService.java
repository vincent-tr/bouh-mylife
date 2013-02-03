package mylife.home.hw.driver;

import java.util.EnumSet;

import mylife.home.hw.api.Device;
import mylife.home.hw.api.DeviceAccessDeniedException;
import mylife.home.hw.api.DeviceManager;
import mylife.home.hw.api.Options;
import mylife.home.hw.driver.device.Manager;
import aQute.bnd.annotation.component.Component;

@Component
public class DeviceManagerService implements DeviceManager {

	@Override
	public Device open(int pinId, EnumSet<Options> options) throws DeviceAccessDeniedException {
		return Manager.getInstance().open(pinId, options);
	}
}
