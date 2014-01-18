package mylife.home.hw.driver.platform;

import java.util.logging.Logger;

import mylife.home.hw.driver.platform.PlatformCalls.pollfd;

public class PlatformFile {

	private static final Logger log = Logger.getLogger(PlatformFile.class
			.getName());

	/**
	 * Check du retour des appels système
	 * 
	 * @param ret
	 * @param name
	 */
	private static void checkReturn(int ret, String name) {
		if (ret >= 0)
			return;
		throw new PlatformException(PlatformError.getByNumber(-ret), name);
	}

	private static void logCall(String msg) {
		log.fine(msg);
		/*
		long tid = Thread.currentThread().getId();
		System.out.println("tid:" + tid + " " + msg);
		System.out.flush();
		*/
	}
	
	private static void logCallReturn(String msg) {
		log.fine(msg);
		/*
		long tid = Thread.currentThread().getId();
		System.out.println("tid:" + tid + " " + msg);
		System.out.flush();
		*/
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
		logCall("PlatformCalls.open(pathname=" + pathname + ", flags=" + flags
				+ ", mode=" + mode + ");");
		int ret = PlatformCalls.open(pathname, flags, mode);
		logCallReturn("PlatformCalls.open = " + ret);
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
		logCall("PlatformCalls.close(fd=" + fd + ");");
		int ret = PlatformCalls.close(fd);
		logCallReturn("PlatformCalls.close = " + ret);
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
		if (buf == null)
			throw new IllegalArgumentException("buf == null");
		logCall("PlatformCalls.read(fd=" + fd + ", buf=" + buf + ");");
		int ret = PlatformCalls.read(fd, buf);
		logCallReturn("PlatformCalls.read = " + ret);
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
		if (buf == null)
			throw new IllegalArgumentException("buf == null");
		logCall("PlatformCalls.write(fd=" + fd + ", buf=" + buf + ");");
		int ret = PlatformCalls.write(fd, buf);
		logCallReturn("PlatformCalls.write = " + ret);
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
		if (fds == null)
			throw new IllegalArgumentException("fds == null");
		logCall("PlatformCalls.poll(fds=" + fds + ", timeout=" + timeout + ");");
		int ret = PlatformCalls.poll(fds, timeout);
		logCallReturn("PlatformCalls.poll = " + ret);
		checkReturn(ret, "poll");
		return ret;
	}

	/**
	 * Ecriture dans un fichier
	 * 
	 * @param fd
	 * @param offset
	 * @param whence
	 * @return
	 */
	private static int lseek(int fd, int offset, int whence) {
		logCall("PlatformCalls.lseek(fd=" + fd + ", offset=" + offset + ", whence=" + whence + ");");
		int ret = PlatformCalls.lseek(fd, offset, whence);
		logCallReturn("PlatformCalls.lseek = " + ret);
		checkReturn(ret, "lseek");
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
	 * 
	 * @param fd
	 * @param flags
	 * @param mode
	 */
	public PlatformFile(String pathname, int flags, int mode) {
		this.fd = open(pathname, flags, mode);
	}

	/**
	 * Ouverture d'un fichier
	 * 
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
		synchronized (fdLock) {
			if (fd > -1) {
				close(fd);
				fd = -1;
			}
		}
	}

	/**
	 * Indique si le fichier est valide
	 * 
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
	 * 
	 * @param buffer
	 * @return
	 */
	public int read(byte[] buffer) {
		synchronized (fdLock) {
			checkValid();
			return read(fd, buffer);
		}
	}

	/**
	 * Ecriture dans un fichier
	 * 
	 * @param buffer
	 */
	public void write(byte[] buffer) {
		synchronized (fdLock) {
			checkValid();
			write(fd, buffer);
		}
	}

	/**
	 * Vérification de l'état de fichiers
	 * 
	 * @param events
	 * @param timeout
	 *            (en ms)
	 */
	public static void poll(PollEvent[] events, int timeout) {
		if (events == null)
			throw new IllegalArgumentException("events == null");
		if(events.length == 0)
			throw new IllegalArgumentException("events.length == 0");

		pollfd[] pfds = new pollfd[events.length];
		for (int index = 0; index < events.length; ++index) {
			pfds[index] = new pollfd();
			pfds[index].fd = events[index].file.fd;
			pfds[index].events = events[index].checkedEvents;
		}

		poll(pfds, timeout);

		for (int index = 0; index < events.length; ++index) {
			events[index].returnedEvents = pfds[index].revents;
		}
	}

	/**
	 * Evenements de poll
	 * 
	 * @author pumbawoman
	 * 
	 */
	public static class PollEvent {

		private final PlatformFile file;
		private final short checkedEvents;
		private short returnedEvents;

		/**
		 * Création de l'entité
		 * 
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
		 * 
		 * @return
		 */
		public short getReturnedEvents() {
			return returnedEvents;
		}

		/**
		 * Obtention du fichier
		 * 
		 * @return
		 */
		public PlatformFile getFile() {
			return file;
		}

		/**
		 * Obtention des evenements à vérifier
		 * 
		 * @return
		 */
		public short getCheckedEvents() {
			return checkedEvents;
		}
	}

	/**
	 * Ecriture dans un fichier
	 * 
	 * @param offset
	 * @param whence
	 * @return
	 */
	public int lseek(int offset, int whence) {
		synchronized (fdLock) {
			checkValid();
			return lseek(fd, offset, whence);
		}
	}
}
