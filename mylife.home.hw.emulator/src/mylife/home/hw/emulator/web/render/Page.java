package mylife.home.hw.emulator.web.render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Gestion d'une page
 * @author pumbawoman
 *
 */
public class Page extends WebBase {

	/**
	 * Titre de la page
	 */
	private String title;
	
	/**
	 * Titre de la page
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Titre de la page
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Icone
	 */
	private String icon;

	/**
	 * Icone
	 * @return
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Contenu
	 */
	private final Collection<WebRendable> content = new ArrayList<WebRendable>();

	/**
	 * Contenu
	 */
	public Collection<WebRendable> getContent() {
		return content;
	}
	
	/**
	 * Icone
	 * @param icon
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	@Override
	public void render(WebStream stream) throws IOException {
		
		stream.writeln("<html>");
		stream.indentInc();
		
		stream.writeln("<head>");
		stream.indentInc();
		if(icon != null && !"".equals(icon))
			stream.writeln("<link rel=\"shortcut icon\" href=\"" + getResourceUrl(encodeHtml(icon)) + "\" />");
		if(title != null && !"".equals(title))
		stream.writeln("<title>" + encodeHtml(title) + "</title>");
		stream.indentDec();
		stream.writeln("</head>");
		
		stream.writeln("<body>");
		stream.indentInc();
		for(WebRendable item : content) {
			item.render(stream);
		}
		stream.indentDec();
		stream.writeln("</body>");
		
		stream.indentDec();
		stream.writeln("</html>");
	}
	
}
