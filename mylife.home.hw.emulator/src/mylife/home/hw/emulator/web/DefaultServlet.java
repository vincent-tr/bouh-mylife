package mylife.home.hw.emulator.web;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import aQute.bnd.annotation.component.Component;

@Component(provide=Servlet.class, properties={"alias="+DefaultServlet.path})
public class DefaultServlet extends HttpServlet {

	/**
	 * Chemin d'accès au servlet
	 */
	public static final String path = "/mylife.home.hw.emulator";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		OutputStreamWriter writer = new OutputStreamWriter(resp.getOutputStream());
		
		String content = getPage();
		writer.write(content);
		writer.flush();
		
	}
	
	private String getPage() {
		return "<html>\n"+
		"\t<head>\n"+
		"\t\t<link rel=\"shortcut icon\" href=\"/mylife.home.hw.emulator/resources/MyLife-128.png\" />\n"+
		"\t\t<link rel=\"stylesheet\" href=\"/mylife.home.hw.emulator/resources/mylife.css\" />\n"+
		"\t\t<script type=\"text/javascript\" src=\"/mylife.home.hw.emulator/resources/bgrefresh.js\"></script>\n"+
		"\t\t<title>MyLife.Home HW Emulator</title>\n"+
		"\t</head>\n"+
		"\t<body onload=\"new BGRefresh();\">\n"+
		"\t\t<table width=\"100%\">\n"+
		"\t\t\t<tr>\n"+
		"\t\t\t\t<td>\n"+
		"\t\t\t\t\t<table align=\"center\">\n"+
		"\t\t\t\t\t\t<tr>\n"+
		"\t\t\t\t\t\t\t<td>\n"+
		"\t\t\t\t\t\t\t\t<img src=\"/mylife.home.hw.emulator/resources/MyLife-48.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t\t</td>\n"+
		"\t\t\t\t\t\t\t<td>\n"+
		"\t\t\t\t\t\t\t\t<h1>MyLife.Home HW Emulator</h1>\n"+
		"\t\t\t\t\t\t\t</td>\n"+
		"\t\t\t\t\t\t</tr>\n"+
		"\t\t\t\t\t</table>\n"+
		"\t\t\t\t</td>\n"+
		"\t\t\t</tr>\n"+
		"\t\t\t<tr>\n"+
		"\t\t\t\t<td align=\"center\">\n"+
		"\t\t\t\t\t<div style=\"position: relative; width: 435px; height: 581px\" class=\"container\">\n"+
		"\t\t\t\t\t\t<img style=\"z-index:-1\" src=\"/mylife.home.hw.emulator/resources/LayoutBack.png\" alt=\"\" />\n"+
		// gauche : 173px , droite : 238px , top = (68 + idx * 36)px
		"\t\t\t\t\t\t<img id=\"pinType3\" style=\"position:absolute; top:104px; left:173px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputLeft.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType5\" style=\"position:absolute; top:140px; left:173px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputLeft.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType7\" style=\"position:absolute; top:176px; left:173px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputLeft.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType8\" style=\"position:absolute; top:176px; left:238px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputRight.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType10\" style=\"position:absolute; top:212px; left:238px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputRight.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType11\" style=\"position:absolute; top:248px; left:173px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputLeft.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType12\" style=\"position:absolute; top:248px; left:238px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputRight.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType13\" style=\"position:absolute; top:284px; left:173px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputLeft.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType15\" style=\"position:absolute; top:320px; left:173px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputLeft.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType16\" style=\"position:absolute; top:320px; left:238px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputRight.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType18\" style=\"position:absolute; top:356px; left:238px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputRight.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType19\" style=\"position:absolute; top:392px; left:173px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputLeft.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType21\" style=\"position:absolute; top:428px; left:173px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputLeft.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType22\" style=\"position:absolute; top:428px; left:238px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputRight.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType23\" style=\"position:absolute; top:464px; left:173px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputLeft.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType24\" style=\"position:absolute; top:464px; left:238px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputRight.png\" alt=\"\" />\n"+
		"\t\t\t\t\t\t<img id=\"pinType26\" style=\"position:absolute; top:500px; left:238px; z-index:1\" src=\"/mylife.home.hw.emulator/resources/AnalogOuputRight.png\" alt=\"\" />\n"+
		"\t\t\t\t\t</div>\n"+
		"\t\t\t\t</td>\n"+
		"\t\t\t</tr>\n"+
		"\t\t</table>\n"+
		"\t</body>\n"+
		"</html>";

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6430967627948050536L;

}
