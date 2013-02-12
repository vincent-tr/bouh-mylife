package mylife.home.hw.emulator.web;

import java.io.IOException;


/**
 * Interface d'un rendu web
 * @author pumbawoman
 *
 */
public interface WebRendable {
	
	/**
	 * Ex�cution du rendu
	 * @param writer
	 */
	public abstract void render(Appendable appender) throws IOException;
	
}
