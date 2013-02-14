package mylife.home.hw.emulator.web.render;

import java.io.IOException;

/**
 * Rendu d'une image
 * @author pumbawoman
 *
 */
public class ImageRender extends WebBase {

	/**
	 * Nom de ressource de l'image
	 */
	private final String image;
	
	/**
	 * Constructeur avec nom de ressource de l'image
	 * @param image
	 */
	public ImageRender(String image) {
		this.image = image;
	}

	@Override
	public void render(WebStream stream) throws IOException {
		stream.writeln("<img src=\"" + getResourceUrl(encodeHtml(image)) + "\" alt=\"\" />");		
	}

}
