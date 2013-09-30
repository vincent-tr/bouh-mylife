package mylife.home.webcomponents;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import mylife.home.net.NetObject;
import mylife.home.net.NetRepository;
import mylife.home.net.NetContainer;


/**
 * Composant de base
 * @author pumbawoman
 *
 */
public class Component extends GenericServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 70136661310299425L;

	private Map<String, String> configuration;
	private NetContainer container;

	/**
	 * Obtention de la configuration
	 * @return
	 */
	public Map<String, String> getConfiguration() {
		return configuration;
	}
	
	/**
	 * Obtention de l'objet publi� correspondant au composant
	 * @return
	 */
	public NetObject getObject() {
		return container.getObject();
	}
	
	/**
	 * Enregistrement de l'objet correspondant au composant. L'objet est ensuite publi� et d�publi� automtiquement
	 * @param object
	 * @param channel
	 */
	protected void registerObject(NetObject object, String channel) {
		if(this.container != null)
			throw new IllegalStateException();
		this.container = NetRepository.register(object, channel, true); 
		this.log(String.format("registering netobject with id '%s' on channel '%s'", container.getObject().getId(), container.getChannel()));
	}
	
	@Override
	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		throw new ServletException("no service");
	}

	@Override
	public final void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		HashMap<String, String> configMap = new HashMap<String, String>();
		for(Enumeration<String> e = config.getInitParameterNames(); e.hasMoreElements();) {
			String name = e.nextElement();
			configMap.put(name, config.getInitParameter(name));
		}
		configuration = Collections.unmodifiableMap(configMap);
		
		this.log(String.format("registering component of type '%s' with name '%s'", this.getClass().toString(), this.getServletName()));
		ComponentRepository.registerComponent(this);
		
		create();
	}

	/**
	 * A impl�menter sur cr�ation au lieu de init
	 */
	protected void create() throws ServletException {
	}
	
	/**
	 * A overrider sur fin de vie
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		if(container != null) {
			this.log(String.format("unregistering netobject with id '%s' on channel '%s'", container.getObject().getId(), container.getChannel()));
			NetRepository.unregister(container);
		}

		this.log(String.format("unregistering component of type '%s' with name '%s'", this.getClass().toString(), this.getServletName()));
		ComponentRepository.unregisterComponent(this);
	}
}
