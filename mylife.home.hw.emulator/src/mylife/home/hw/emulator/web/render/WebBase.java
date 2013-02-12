package mylife.home.hw.emulator.web.render;

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
}
