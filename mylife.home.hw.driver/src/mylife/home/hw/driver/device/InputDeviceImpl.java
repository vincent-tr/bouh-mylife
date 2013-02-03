package mylife.home.hw.driver.device;

import java.io.File;
import java.util.EnumSet;

import mylife.home.hw.api.InputDevice;
import mylife.home.hw.api.InputDeviceListener;
import mylife.home.hw.api.Options;

public class InputDeviceImpl extends SysFSDeviceImpl implements InputDevice {

	public InputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options, "/sys/class/gpio", "export", "unexport", "gpio");
		try {
			write(getItemDirectoryPath() + File.separator + "direction", "in");
			write(getItemDirectoryPath() + File.separator + "edge", "both");
			// TODO : pull resistors 
			if(options.contains(Options.OPTION_PULL_DOWN) || options.contains(Options.OPTION_PULL_UP))
				throw new UnsupportedOperationException("Pull resistors unsupported now");
			
			// TODO : abonnement reads
		} catch (Exception ex) {
			reset();
			throw ex;
		}
	}

	@Override
	public boolean getValue() {
		// TODO
	}

	@Override
	public void addListener(InputDeviceListener listener) {

		if (listener == null)
			throw new IllegalArgumentException("listener is null");

		// TODO
	}

	@Override
	public void removeListener(InputDeviceListener listener) {

		if (listener == null)
			throw new IllegalArgumentException("listener is null");

		// TODO
	}
}
