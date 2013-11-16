package mylife.home.netlog;

import mylife.home.net.api.NetComponent;
import mylife.home.net.api.NetComponentFactory;

import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

@Component(
		provide=Object.class, 
		configurationPolicy=ConfigurationPolicy.require,
		designate=Logger.Configuration.class,
		immediate=true)
public class Logger {

	@Meta.OCD(name="MyLife.Home NetLog")
	public interface Configuration {
		@Meta.AD(name="Component ID")
		String id();
		
		@Meta.AD(name="Component display")
		String display();

		@Meta.AD(
				name="Max log level",
				deflt="4",
				optionLabels={"ERROR","WARNING","INFO","DEBUG"},
				optionValues={"1","2","3","4"})
		int maxLevel();
	}
	
	private LogReaderService logReaderService;
	private NetComponentFactory netManager;
	private Configuration configuration;
	private NetComponent net;
	private Listener listener;
	
	@Reference
	public void setNetManager(NetComponentFactory netManager) {
		this.netManager = netManager;
	}
	
	@Reference
	public void setLogReaderService(LogReaderService logReaderService)  {
		this.logReaderService = logReaderService;
	}

	/**
	 * Activation
	 * @param ctx
	 */
	@Activate
	public void activate(ComponentContext ctx) {
		configuration = (Configuration)Configurable.createConfigurable(Configuration.class, ctx.getProperties());
		initialize();
	}

	/**
	 * Désactivation
	 * @param ctx
	 */
	@Deactivate
	public void deactivate(ComponentContext ctx) {
		terminate();
	}
	
	/**
	 * Modification de configuration
	 * @param ctx
	 */
	@Modified
	public void modified(ComponentContext ctx) {
		terminate();
		configuration = (Configuration)Configurable.createConfigurable(Configuration.class, ctx.getProperties());
		initialize();
	}

	/**
	 * Libération du composant
	 */
	private void terminate() {
		if(listener != null)
			logReaderService.removeLogListener(listener);
		if(net != null)
			net.close();
	}
	
	/**
	 * Initialisation du composant
	 */
	private void initialize() {
		
		net = netManager.createComponent(configuration.id(), configuration.display(), Logger.class.getSimpleName());
		logReaderService.addLogListener(listener = new Listener(this));
	}
	
	/**
	 * Ajout de log
	 * @param entry
	 */
	private void logged(LogEntry entry) {
		
		String level = ""+entry.getLevel();
		switch(entry.getLevel()) {
			case LogService.LOG_ERROR:
				level = "ERROR";
				break;
			case LogService.LOG_WARNING:
				level = "WARNING";
				break;
			case LogService.LOG_INFO:
				level = "INFO";
				break;
			case LogService.LOG_DEBUG:
				level = "DEBUG";
				break;
		}
		
		String source = "<unknown>";
		Bundle bundle = entry.getBundle();
		if(bundle != null)
			source = bundle.getSymbolicName();
		
		String msg = String.format("%s : %s - %s", source, level, entry.getMessage());
		net.sendMessage(msg);
	}
	
	/**
	 * Implémentation de LogListener
	 * @author pumbawoman
	 *
	 */
	private static class Listener implements LogListener {
		private final Logger logger;
		public Listener(Logger logger) {
			this.logger = logger;
		}
		@Override
		public void logged(LogEntry entry) {
			logger.logged(entry);
		}
	}
}
