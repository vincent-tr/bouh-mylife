package mylife.home.hw.emulator.web.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import mylife.home.hw.emulator.web.ResourceServlet;

/**
 * Ojet web de base
 * @author pumbawoman
 *
 */
public abstract class WebBase implements WebRendable {

	/**
	 * Obtention de l'url d'une ressource
	 * @param resourceName
	 * @return
	 */
	protected String getResourceUrl(String resourceName) {
		return ResourceServlet.path + "/" + resourceName;
	}
	
	/**
	 * Escape d'html
	 * @param s
	 * @return
	 */
	protected static String encodeHtml(String s)
	{
	    StringBuffer out = new StringBuffer();
	    for(int i=0; i<s.length(); i++)
	    {
	        char c = s.charAt(i);
	        if(c > 127 || c=='"' || c=='<' || c=='>')
	        {
	           out.append("&#"+(int)c+";");
	        }
	        else
	        {
	            out.append(c);
	        }
	    }
	    return out.toString();
	}
	
	/**
	 * Attributs
	 */
	private final Map<String, String> attributes = new HashMap<String, String>();
	
	/**
	 * Attributs
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}
	
	/**
	 * Ajout d'attributs
	 * @param key
	 * @param value
	 * @return
	 */
	public WebBase putAttribute(String key, String value) {
		getAttributes().put(key, value);
		return this;
	}
	
	/**
	 * Formattage des attributs
	 * @return
	 */
	protected String formatAttributes() {
		
		StringBuilder builder = new StringBuilder();
		for(Entry<String, String> attribute : attributes.entrySet()) {
			builder.append(" " + attribute.getKey() + "=\"" + attribute.getValue() + "\"");
		}
		return builder.toString();
	}
}
