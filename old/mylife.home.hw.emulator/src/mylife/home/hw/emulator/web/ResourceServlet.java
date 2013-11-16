package mylife.home.hw.emulator.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import aQute.bnd.annotation.component.Component;

@Component(provide=Servlet.class, properties={"alias="+ResourceServlet.path})
public class ResourceServlet extends HttpServlet {

	/**
	 * Chemin relatif
	 */
	private static final String relativePath = "/resources";
	
	/**
	 * Chemin d'accès au servlet
	 */
	public static final String path = DefaultServlet.path + relativePath;
	
	private static final Map<String, String> mimeMap;
	
	static {
		mimeMap = new HashMap<String, String>();
		mimeMap.put("png", "image/png");
		mimeMap.put("js", "application/javascript");
		mimeMap.put("css", "text/css");
	}
			
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// getRequestURI : /mylife.home.hw.emulator/zouave
		String uri = req.getRequestURI();
		if(uri.length() <= path.length())
			throw new IllegalArgumentException("bad uri : " + uri);
		Class<?> thisClass = this.getClass();
		String pack = thisClass.getPackage().getName().replace('.', '/');
		String resourcePath = "/" + pack + relativePath + uri.substring(path.length());
		
		InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
		if(inputStream == null)
			throw new IllegalArgumentException("resource note found : " + resourcePath);

		// Selection du mime type en fonction de l'extension
		int index = resourcePath.lastIndexOf('.');
		if(index > -1) {
			String ext = resourcePath.substring(index);
			String mime = mimeMap.get(ext);
			if(mime != null)
				resp.setContentType(mime);
		}
		
		OutputStream outputStream = resp.getOutputStream();
		
		byte[] buf = new byte[8192];
		while (true) {
			int length = inputStream.read(buf);
			if (length < 0)
				break;
			outputStream.write(buf, 0, length);
		}
		outputStream.flush();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1003572626829250753L;

}
