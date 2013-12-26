package org.mylife.home.ui.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.mylife.home.ui.structure.Structure;

public class WebImage extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4513871873875504543L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		dispatch(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		dispatch(req, resp);
	}

	private void dispatch(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String id = req.getParameter("id");
		if(StringUtils.isEmpty(id)) {
			throw new ServletException("Invalid id");
		}
		InputStream is = Structure.getImage(id);
		if(is == null) {
			throw new ServletException("Invalid id");
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(is, baos);
		byte[] image = baos.toByteArray();
		
		String mime = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(image));
		if(mime == null)
			mime = "image/png"; // arbitraire
		
		resp.setContentType(mime);
		IOUtils.copy(new ByteArrayInputStream(image), resp.getOutputStream());
	}
}
