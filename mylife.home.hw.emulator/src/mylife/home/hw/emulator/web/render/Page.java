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
	 * Icone
	 * @param icon
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * Style
	 */
	private String style;

	/**
	 * Style
	 * @return
	 */
	public String getStyle() {
		return style;
	}
	
	/**
	 * Style
	 * @param style
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 * Scripts
	 */
	private final Collection<String> scripts = new ArrayList<String>();

	/**
	 * Scripts
	 */
	public Collection<String> getScripts() {
		return scripts;
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
	
	@Override
	public void render(WebStream stream) throws IOException {
		
		stream.writeln("<html>");
		stream.indentInc();
		
		stream.writeln("<head>");
		stream.indentInc();
		if(icon != null && !"".equals(icon))
			stream.writeln("<link rel=\"shortcut icon\" href=\"" + getResourceUrl(encodeHtml(icon)) + "\" />");
		if(style != null && !"".equals(style))
			stream.writeln("<link rel=\"stylesheet\" href=\"" + getResourceUrl(encodeHtml(style)) + "\" />");
		for(String script : scripts)
			stream.writeln("<script type=\"text/javascript\" src=\"" + getResourceUrl(encodeHtml(script)) + "\" />");
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
