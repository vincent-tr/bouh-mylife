package mylife.home.hw.driver.device;

/**
 * Gestion du poll des device en input
 * @author pumbawoman
 *
 */
public class PollingService {

	/**
	 * singleton
	 */
	private PollingService() {
	}

	/**
	 * singleton
	 */
	private static final PollingService instance = new PollingService();

	/**
	 * singleton
	 * @return
	 */
	public static PollingService getInstance() {
		return instance;
	}

	/**
	 * Démarrage du service
	 */
	public void start() {
		
	}
	
	/**
	 * Arrêt du service
	 */
	public void stop() {
		
	}
}
