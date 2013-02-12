package mylife.home.hw.emulator.web;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mylife.home.hw.emulator.web.render.Page;
import mylife.home.hw.emulator.web.render.StringRender;
import mylife.home.hw.emulator.web.render.WebStream;
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
		
		// getRequestURI : /mylife.home.hw.emulator/zouave
		// getQueryString : toto=toto&tata=tata
/*		
		OutputStreamWriter w = new OutputStreamWriter( resp.getOutputStream());
		
		w.write("<html><head/><body>");
		
		for(Method m : req.getClass().getMethods()) {
			if(!m.getReturnType().equals(String.class))
				continue;
			if(m.getParameterTypes().length > 0)
				continue;
			String value = null;
			try {
				value = (String)m.invoke(req);
			}catch(Exception ex) {
				value = ex.toString();
			}
			
			w.write(m.getName() + " : " + value + "</br>");
		}
		
		w.write("</body></html>");
		
		w.flush();
*/
		OutputStreamWriter writer = new OutputStreamWriter( resp.getOutputStream());
		WebStream stream = new WebStream(writer);
		
		Page page = new Page();
		page.setTitle("MyLife.Home HW Emulator");
		page.setIcon("MyLife-128.png");
		page.getContent().add(new StringRender("<h1>MyLife.Home HW Emulator</h1>"));
		
		page.render(stream);
		writer.flush();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6430967627948050536L;

}
