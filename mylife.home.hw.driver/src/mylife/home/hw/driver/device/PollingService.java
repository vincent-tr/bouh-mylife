package mylife.home.hw.driver.device;

import java.util.HashSet;
import java.util.Set;

import mylife.home.hw.driver.platform.PlatformFile;

/**
 * Gestion du poll des device en input
 * 
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
	 * 
	 * @return
	 */
	public static PollingService getInstance() {
		return instance;
	}

	/**
	 * Démarrage du service
	 */
	public void start() {
		worker.start();
	}

	/**
	 * Arrêt du service
	 * 
	 * @throws InterruptedException
	 */
	public void stop() throws InterruptedException {
		worker.terminate();
	}

	/**
	 * Thread de fonctionnement
	 */
	private final Worker worker = new Worker();

	/**
	 * Thread de fonctionnement
	 */
	private static class Worker extends Thread {

		/**
		 * Flag de sortie
		 */
		private boolean exit;

		/**
		 * Constructeur par défaut
		 */
		public Worker() {
		}

		/**
		 * Arrêt du thread
		 * 
		 * @throws InterruptedException
		 */
		public void terminate() throws InterruptedException {
			exit = true;
			this.join();
		}

		/**
		 * Execution du worker
		 */
		@Override
		public void run() {
			while (!exit)
				doWork();
		}

		/**
		 * Exéuction d'une passe
		 */
		private void doWork() {

			final Set<Pollable> pollables = PollingService.instance.pollables;
			PlatformFile.PollEvent[] events = null;

			// création de la structure
			synchronized (pollables) {
				events = new PlatformFile.PollEvent[pollables.size()];
				int index = 0;
				for (Pollable pollable : pollables) {
					events[index++] = new PlatformFile.PollEvent(
							pollable.getFile(), pollable.getCheckedEvents());
				}
			}

			// exécution d'un poll
			PlatformFile.poll(events, 100);

			// lecture des evenements
			synchronized (pollables) {
				for (PlatformFile.PollEvent event : events) {
					short revents = event.getReturnedEvents();
					if (revents == 0)
						continue;
					
					// sinon ca a été checké, on cherche le fichier dans le set
					for (Pollable pollable : pollables) {
						if(pollable.getFile().equals(event.getFile())) {
							pollable.setEvents(revents);
							break;
						}
					}
					// si pas trouvé, c'est que le pollable a été retiré entre temps, on ne fait rien
				}
			}
		}
	}

	/**
	 * Liste des pollables gérés
	 */
	private final Set<Pollable> pollables = new HashSet<Pollable>();

	/**
	 * Ajout d'un pollable
	 * 
	 * @param pollable
	 */
	public void addPollable(Pollable pollable) {
		synchronized (pollables) {
			pollables.add(pollable);
		}
	}

	/**
	 * Retrait d'un pollable
	 * 
	 * @param pollable
	 */
	public void removePollable(Pollable pollable) {
		synchronized (pollables) {
			pollables.remove(pollable);
		}
	}
}
