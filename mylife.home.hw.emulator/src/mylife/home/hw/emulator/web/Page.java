package mylife.home.hw.emulator.web;

import java.io.IOException;

/**
 * Gestion d'une page
 * @author pumbawoman
 *
 */
public class Page implements WebRendable {

	@Override
	public void render(Appendable appender) throws IOException {
		appender.append("<html>\n");
		appender.append("\t<head>\n");
		appender.append("\t</head>\n");
		appender.append("\t<body>\n");
		appender.append("\t</body>\n");
		appender.append("</html>\n");
	}
	
}
