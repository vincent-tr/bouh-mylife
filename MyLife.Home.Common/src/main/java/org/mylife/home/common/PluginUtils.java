package org.mylife.home.common;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class PluginUtils {

	private static final Logger logger = Logger.getLogger(PluginUtils.class
			.getName());
	
	/**
	 * Pas d'instance
	 */
	private PluginUtils() {
	}

	/**
	 * Obtention de toutes les classes du package qui héritent/implémentent la class spécifiée
	 * @param packageName
	 * @param superClass
	 * @return
	 * @throws IOException
	 */
	public static Set<Class<?>> listClassesInPackageAssignableFrom(String packageName, Class<?> superClass) throws IOException {
		Set<Class<?>> packageClasses = listClassesInPackage(packageName);
		Set<Class<?>> classes = new HashSet<Class<?>>();

		for(Class<?> clazz : packageClasses) {

			// On verifie si la classe correspond a une commande
			if (!superClass.isAssignableFrom(clazz))
				continue;

			classes.add(clazz);
		}

		return classes;
	}
	
	// http://ricardozuasti.com/2012/list-all-classes-in-a-package-even-from-a-jar-file/
	
	/**
	 * Obtention de toutes les classes du package
	 * @param packageName
	 * @return
	 * @throws IOException
	 */
	public static Set<Class<?>> listClassesInPackage(String packageName)
			throws IOException {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		ArrayList<String> dirs = new ArrayList<String>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(URLDecoder.decode(resource.getFile(), "UTF-8"));
		}
		Set<String> classes = new TreeSet<String>();
		for (String directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		Set<Class<?>> classList = new HashSet<Class<?>>();
		for (String clazz : classes) {
			try {
			classList.add(Class.forName(clazz));
			} catch(ClassNotFoundException e) {
				// ne devrait pas arriver
				// on log et on ignore
				logger.log(Level.WARNING, "Class not found in lookup", e);
			}
		}
		return classList;
	}

	private static Set<String> findClasses(String path, String packageName)
			throws MalformedURLException, IOException {
		Set<String> classes = new TreeSet<String>();
		if (path.startsWith("file:") && path.contains("!")) {
			String[] split = path.split("!");
			URL jar = new URL(split[0]);
			ZipInputStream zip = new ZipInputStream(jar.openStream());
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (entry.getName().endsWith(".class")) {
					String className = entry.getName().replaceAll("[$].*", "")
							.replaceAll("[.]class", "").replace('/', '.');
					if (className.startsWith(packageName)) {
						classes.add(className);
					}
				}
			}
		}
		File dir = new File(path);
		if (!dir.exists()) {
			return classes;
		}
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file.getAbsolutePath(), packageName
						+ "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				String className = packageName
						+ '.'
						+ file.getName().substring(0,
								file.getName().length() - 6);
				classes.add(className);
			}
		}
		return classes;
	}
}
