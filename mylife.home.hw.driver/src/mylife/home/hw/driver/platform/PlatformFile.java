package mylife.home.hw.driver.platform;

import mylife.home.hw.driver.platform.PlatformCalls.pollfd;

public class PlatformFile {

	/**
	 * Check du retour des appels système
	 * @param ret
	 * @param name
	 */
	private static void checkReturn(int ret, String name) {
		if(ret >= 0)
			return;
		throw new PlatformException(PlatformError.getByNumber(-ret), name);
	}
	
	/**
	 * Appel système open
	 * 
	 * @param pathname
	 * @param flags
	 * @param mode
	 * @return
	 */
	private static int open(String pathname, int flags, int mode) {
		int ret = PlatformCalls.open(pathname, flags, mode);
		checkReturn(ret, "open");
		return ret;
	}

	/**
	 * Appel système close
	 * 
	 * @param fd
	 * @return
	 */
	private static void close(int fd) {
		int ret = PlatformCalls.close(fd);
		checkReturn(ret, "close");
	}

	/**
	 * Appel sysème read
	 * 
	 * @param fd
	 * @param buf
	 * @return
	 */
	private static int read(int fd, byte[] buf) {
		if(buf == null)
			throw new IllegalArgumentException("buf == null");
		int ret = PlatformCalls.read(fd, buf);
		checkReturn(ret, "read");
		return ret;
	}

	/**
	 * Appel système write
	 * 
	 * @param fd
	 * @param buf
	 * @return
	 */
	private static int write(int fd, byte[] buf) {
		if(buf == null)
			throw new IllegalArgumentException("buf == null");
		int ret = PlatformCalls.write(fd, buf);
		checkReturn(ret, "write");
		return ret;
	}

	/**
	 * Appel sysème poll
	 * 
	 * @param fds
	 * @param timeout
	 * @return
	 */
	private static int poll(PlatformCalls.pollfd[] fds, int timeout) {
		if(fds == null)
			throw new IllegalArgumentException("fds == null");
		int ret = PlatformCalls.poll(fds, timeout);
		checkReturn(ret, "poll");
		return ret;
	}
	
	/**
	 * File descriptor du fichier
	 */
	private int fd;
	
	/**
	 * Verrou d'accès
	 */
	private final Object fdLock = new Object();
	
	/**
	 * Ouverture d'un fichier
	 * @param fd
	 * @param flags
	 * @param mode
	 */
	public PlatformFile(String pathname, int flags, int mode) {
		this.fd = open(pathname, flags, mode);
	}
	
	/**
	 * Ouverture d'un fichier
	 * @param fd
	 * @param flags
	 */
	public PlatformFile(String pathname, int flags) {
		this(pathname, flags, 0);
	}

	/**
	 * Ferme le fichier
	 */
	public void close() {
		synchronized(fdLock) {
			if(fd > -1) {
				close(fd);
				fd = -1;
			}
		}
	}
	
	/**
	 * Indique si le fichier est valide
	 * @return
	 */
	public boolean isValid() {
		return fd > -1;
	}
	
	/**
	 * Vérifie que le fichier soit valide
	 */
	protected void checkValid() {
		if (!isValid())
		throw new java.lang.IllegalStateException("file not valid"); 
	}
	
	/**
	 * Finalizer : fermeture du fichier
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	
	/**
	 * Lecture dans le fichier
	 * @param buffer
	 * @return
	 */
	public int read(byte[] buffer) {
		synchronized(fdLock) {
			checkValid();
			return read(fd, buffer);
		}
	}
	
	/**
	 * Ecriture dans un fichier
	 * @param buffer
	 */
	public void write(byte[] buffer) {
		synchronized(fdLock) {
			checkValid();
			write(fd, buffer);
		}
	}
	
	/**
	 * Vérification de l'état de fichiers
	 * @param events
	 * @param timeout (en ms)
	 */
	public static void poll(PollEvent[] events, int timeout) {
		if(events == null)
			throw new IllegalArgumentException("events == null");
		
		pollfd[] pfds = new pollfd[events.length];
		for(int index=0; index<events.length; ++index) {
			pfds[index].fd = events[index].file.fd;
			pfds[index].events = events[index].checkedEvents;
		}
		
		poll(pfds, timeout);

		for(int index=0; index<events.length; ++index) {
			events[index].returnedEvents = pfds[index].revents;
		}
	}
	
	/**
	 * Evenements de poll
	 * @author pumbawoman
	 *
	 */
	public static class PollEvent {
		
		private final PlatformFile file;
		private final short checkedEvents;
		private short returnedEvents;
		
		/**
		 * Création de l'entité
		 * @param file
		 * @param checkedEvents
		 */
		public PollEvent(PlatformFile file, short checkedEvents) {
			this.file = file;
			this.checkedEvents = checkedEvents;
			this.returnedEvents = 0;
		}

		/**
		 * Obtention des evenements qui se sont produits
		 * @return
		 */
		public short getReturnedEvents() {
			return returnedEvents;
		}

		/**
		 * Obtention du fichier
		 * @return
		 */
		public PlatformFile getFile() {
			return file;
		}

		/**
		 * Obtention des evenements à vérifier
		 * @return
		 */
		public short getCheckedEvents() {
			return checkedEvents;
		}
	}
}
