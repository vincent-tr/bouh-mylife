package mylife.home.hw.driver;

import mylife.home.hw.driver.device.PollingService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		PollingService.getInstance().start();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		PollingService.getInstance().stop();
	}
}