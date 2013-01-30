package mylife.home.hw.emulator;

import mylife.home.hw.emulator.web.WebManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private WebManager webManager;
	
	@Override
	public void start(BundleContext context) throws Exception {
		webManager = new WebManager(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		webManager.close();
	}

}