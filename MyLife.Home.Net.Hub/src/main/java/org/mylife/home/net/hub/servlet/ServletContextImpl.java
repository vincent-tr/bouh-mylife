package org.mylife.home.net.hub.servlet;

import java.io.InputStream;
import java.net.URL;
import java.util.Set;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;

import org.apache.log4j.Logger;

public class ServletContextImpl implements ServletContext {
	private final String name;
	private final Properties parameters;
	private final Hashtable attributes = new Hashtable();
	private final Logger logger;

	public ServletContextImpl(String name, Properties parameters) {
		this.name = name;
		this.parameters = parameters;
		logger = Logger.getLogger("jircd.servlet.context."+name);
	}
	public String getInitParameter(String name) {
		return parameters.getProperty(name);
	}
	public Enumeration getInitParameterNames() {
		return parameters.propertyNames();
	}
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	public Enumeration getAttributeNames() {
		return attributes.keys();
	}
	public void setAttribute(String name, Object object) {
		attributes.put(name, object);
	}
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	public void log(String msg) {
		logger.info(msg);
	}
	public void log(Exception exception, String msg) {
		logger.info(msg, exception);
	}
	public void log(String msg, Throwable throwable) {
		logger.info(msg, throwable);
	}

	public ServletContext getContext(String path) {
		return null;
	}
	public Set getResourcePaths(String path) {
		return null;
	}
	public URL getResource(String path) {
		return null;
	}
	public InputStream getResourceAsStream(String path) {
		return null;
	}
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}
	public RequestDispatcher getNamedDispatcher(String name) {
		return null;
	}

	public String getMimeType(String file) {
		return null;
	}
	public String getRealPath(String path) {
		return null;
	}
	public String getServerInfo() {
		return "jIRCd/" + jIRCd.VERSION_MAJOR + '.' + jIRCd.VERSION_MINOR + '.' + jIRCd.VERSION_PATCH;
	}
	public int getMajorVersion() {
		return 2;
	}
	public int getMinorVersion() {
		return 2;
	}
	public String getServletContextName() {
		return name;
	}

	public Servlet getServlet(String name) {
		return null;
	}
	public Enumeration getServlets() {
		return new EmptyEnumeration();
	}
	public Enumeration getServletNames() {
		return new EmptyEnumeration();
	}
}
