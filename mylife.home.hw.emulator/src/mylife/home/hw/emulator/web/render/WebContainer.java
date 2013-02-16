package mylife.home.hw.emulator.web.render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Conteneur
 * @author pumbawoman
 */
public abstract class WebContainer<ContentType extends WebRendable> extends WebBase {

	/**
	 * Tag
	 */
	private final String tag;

	/**
	 * Contenu
	 */
	private final Collection<ContentType> content = new ArrayList<ContentType>();

	/**
	 * Contenu
	 */
	public Collection<ContentType> getContent() {
		return content;
	}
	
	protected WebContainer(String tag) {
		this.tag = tag;
	}

	@Override
	public void render(WebStream stream) throws IOException {
		stream.writeln("<" + tag + formatAttributes() + ">");
		stream.indentInc();
		for(WebRendable item : content) {
			item.render(stream);
		}
		stream.indentDec();
		stream.writeln("</" + tag + ">");
	}
}
