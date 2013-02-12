package mylife.home.hw.emulator.web.render;

import java.io.IOException;


/**
 * Rendu d'une chaine
 * @author pumbawoman
 *
 */
public class StringRender implements WebRendable {

	/**
	 * Valeur
	 */
	private final String value;
	
	/**
	 * Constructeur avec valeur
	 * @param value
	 */
	public StringRender(String value) {
		this.value = value;
	}

	@Override
	public void render(WebStream stream) throws IOException {
		stream.writeln(value);
	}
	
}
