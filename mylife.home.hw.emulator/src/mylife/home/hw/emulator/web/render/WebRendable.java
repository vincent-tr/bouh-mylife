package mylife.home.hw.emulator.web.render;

import java.io.IOException;



/**
 * Interface d'un rendu web
 * @author pumbawoman
 *
 */
public interface WebRendable {
	
	/**
	 * Exécution du rendu
	 * @param stream
	 */
	public abstract void render(WebStream stream) throws IOException;
	
}
