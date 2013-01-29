package mylife.home.hw.emulator;

import java.util.EnumSet;

import mylife.home.hw.api.Device;
import mylife.home.hw.api.DeviceAccessDeniedException;
import mylife.home.hw.api.DeviceManager;
import mylife.home.hw.api.Options;
import aQute.bnd.annotation.component.Component;

public class EmulatorManagerService {
	@Component
	public class DeviceManagerService implements DeviceManager {

		@Override
		public Device open(int pinId, EnumSet<Options> options) throws DeviceAccessDeniedException {
			return Manager.getInstance().open(pinId, options);
		}
	}

}
