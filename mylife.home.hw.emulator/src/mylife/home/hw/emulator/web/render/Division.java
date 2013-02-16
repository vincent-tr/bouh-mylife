package mylife.home.hw.emulator.web.render;

/**
 * div
 * @author pumbawoman
 *
 */
public class Division extends WebContainer<WebRendable> {
	public Division(WebRendable ... content) {
		super("div");
		for(WebRendable item : content)
			this.getContent().add(item);
	}
}
