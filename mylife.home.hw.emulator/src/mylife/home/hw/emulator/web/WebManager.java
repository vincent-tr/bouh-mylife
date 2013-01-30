package mylife.home.hw.emulator.web;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

public class WebManager {

	class HttpServiceTracker extends ServiceTracker {

		public HttpServiceTracker(BundleContext context) {
			super(context, HttpService.class.getName(), null);
		}

		@Override
		public Object addingService(ServiceReference reference) {
            Object service = super.addingService(reference);
            if (service instanceof HttpService)
            {
                osgiManager.bindHttpService((HttpService) service);
            }
            return service;
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {
            if (service instanceof HttpService)
            {
                osgiManager.unbindHttpService((HttpService) service);
            }

            super.removedService(reference, service);
		}
		
	}
	
	public WebManager(BundleContext context) throws Exception {
		
	}
	
	public void close() throws Exception {
		
	}
	

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
