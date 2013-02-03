package mylife.home.hw.driver.layout;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestion du layout
 * @author pumbawoman
 *
 */
public class LayoutManager {

	private final Map<String, PinLayout> layoutMap = initLayoutMap();

	private Map<String, PinLayout> initLayoutMap() {

		final PinLayout rev1Pins = new PinLayoutRev1();
		final PinLayout rev2Pins = new PinLayoutRev2();
		
		Map<String, PinLayout> map = new HashMap<String, PinLayout>();
		
		map.put("0002",rev1Pins);
		map.put("0003",rev2Pins);
		map.put("0004",rev2Pins);
		map.put("0005",rev2Pins);
		map.put("0006",rev2Pins);
		map.put("000f",rev2Pins);
		
		return map;
	}
	
	/**
	 * singleton
	 */
	private LayoutManager() {
		
		// Obtention de la révision
		LineNumberReader reader = new LineNumberReader(new FileReader("/proc/cpuinfo"));
		String line;
		while((line = reader.readLine()) != null) {
			if(line.toLowerCase().startsWith("revision"))
				break;
		}
		reader.close();
		
		if(line == null)
			throw new UnsupportedOperationException("Unable to find revision number in /proc/cpuinfo");
		
		// obtention du numéro
		String rev = line.substring(line.length() - 4);
		
		// TODO
	}
	
	/**
	 * singleton
	 */
	private static final LayoutManager instance = new LayoutManager();

	/**
	 * singleton
	 * @return
	 */
	public static LayoutManager getInstance() {
		return instance;
	}
	
	/**
	 * Obtention du layout des pins
	 * @return
	 */
	public PinLayout getPinLayout() {
		// https://projects.drogon.net/raspberry-pi/wiringpi/pins/
		
		
	}
	
}
