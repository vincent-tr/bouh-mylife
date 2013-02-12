package mylife.home.hw.emulator.web.render;

import java.io.IOException;



/**
 * Interface d'un rendu web
 * @author pumbawoman
 *
 */
public interface WebRendable {
	
	/**
	 * Ex�cution du rendu
	 * @param stream
	 */
	public abstract void render(WebStream stream) throws IOException;
	
}
