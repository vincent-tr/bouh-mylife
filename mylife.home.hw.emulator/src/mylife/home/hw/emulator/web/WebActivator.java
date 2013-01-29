package mylife.home.hw.emulator.web;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

public class WebActivator implements BundleActivator {

	private HttpService getService(BundleContext context) throws Exception {
		String name = HttpService.class.getName();
		ServiceReference reference = context.getServiceReference(name);
		if (reference == null)
			return null;
		return (HttpService) context.getService(reference);
	}

	//private static final String alias = "/mylife.home.hw.emulator";
	private static final String alias = "/mylife";
	
	@Override
	public void start(BundleContext context) throws Exception {
		HttpService service = getService(context);
		if(service != null)
			service.registerServlet(alias, new WebServlet(), null, null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		HttpService service = getService(context);
		if(service != null)
			service.unregister(alias);
	}
}
