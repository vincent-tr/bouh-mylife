package mylife.home.hw.driver.layout;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestion du layout
 * 
 * @author pumbawoman
 * 
 */
public class LayoutManager {

	/**
	 * singleton
	 */
	private LayoutManager() {
	}

	/**
	 * singleton
	 */
	private static final LayoutManager instance = new LayoutManager();

	/**
	 * singleton
	 * 
	 * @return
	 */
	public static LayoutManager getInstance() {
		return instance;
	}

	private final Map<String, Integer> layoutMap = initLayoutMap();

	private Map<String, Integer> initLayoutMap() {

		Map<String, Integer> map = new HashMap<String, Integer>();

		map.put("0002", new Integer(1));
		map.put("0003", new Integer(2));
		map.put("0004", new Integer(2));
		map.put("0005", new Integer(2));
		map.put("0006", new Integer(2));
		map.put("000f", new Integer(2));

		return map;
	}

	/**
	 * Verrou pour révision
	 */
	private final Object revisionLock = new Object();

	/**
	 * Révision en cache
	 */
	private int revision;

	/**
	 * Obtention de la révision
	 * 
	 * @return
	 */
	private int getRevision() {
		if (revision == 0) {
			synchronized (revisionLock) {
				if (revision == 0) {
					try {
						// Obtention de la révision
						LineNumberReader reader = new LineNumberReader(
								new FileReader("/proc/cpuinfo"));
						String line;
						while ((line = reader.readLine()) != null) {
							if (line.toLowerCase().startsWith("revision"))
								break;
						}
						reader.close();

						if (line == null)
							throw new UnsupportedOperationException(
									"Unable to find revision number in /proc/cpuinfo");

						// obtention du numéro
						String rev = line.substring(line.length() - 4);
						Integer irev = layoutMap.get(rev);
						if (irev == 0)
							throw new UnsupportedOperationException(
									"Unsupported revision number");
						revision = irev.intValue();
					} catch (IOException ex) {
						throw new RuntimeException(
								"Unable to find revision number", ex);
					}
				}
			}
		}

		return revision;
	}

	/**
	 * Verrou d'initialisation
	 */
	private final Object pinLayoutLock = new Object();

	/**
	 * Layout en cache
	 */
	private PinLayout pinLayout;

	/**
	 * Obtention du layout des pins
	 * 
	 * @return
	 */
	public PinLayout getPinLayout() {
		if (pinLayout == null) {
			synchronized (pinLayoutLock) {
				if (pinLayout == null) {
					int rev = getRevision();

					switch (rev) {
					case 1:
						pinLayout = new PinLayoutRev1();
						break;
					case 2:
						pinLayout = new PinLayoutRev2();
						break;
					default:
						throw new UnsupportedOperationException(
								"Unsupported revision number");
					}
				}
			}
		}
		return pinLayout;
	}

}
