package mylife.home.hw.driver.platform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Appels de la plateforme
 * 
 * @author pumbawoman
 * 
 */
class PlatformCalls {

	static {
		try {
			// obtention de l'url de la lib
			URL resourceUrl = PlatformCalls.class
					.getResource("PlatformCalls.so");

			// création d'un fichier temp pour la lib
			File tempFile = File.createTempFile("PlatformCalls", ".so");
			tempFile.deleteOnExit();

			// extraction
			InputStream inputStream = resourceUrl.openStream();
			OutputStream outputStream = new FileOutputStream(tempFile);

			byte[] buf = new byte[8192];
			while (true) {
				int length = inputStream.read(buf);
				if (length < 0)
					break;
				outputStream.write(buf, 0, length);
			}
			outputStream.flush();
			outputStream.close();
			inputStream.close();

			// chargement
			System.load(tempFile.getAbsolutePath());

		} catch (IOException ex) {
			throw new RuntimeException(
					"Initializer error while loading PlatformCalls.so",
					ex);
		}
	}

	/**
	 * Structure pour poll
	 * 
	 * @author pumbawoman
	 * 
	 */
	public static class pollfd {
		public int fd; /* file descriptor */
		public short events; /* requested events */
		public short revents; /* returned events */
	}

	/**
	 * Appel système open
	 * 
	 * @param pathname
	 * @param flags
	 * @param mode
	 * @return
	 */
	public static native int open(String pathname, int flags, int mode);

	/**
	 * Appel système close
	 * 
	 * @param fd
	 * @return
	 */
	public static native int close(int fd);

	/**
	 * Appel sysème read
	 * 
	 * @param fd
	 * @param buf
	 * @return
	 */
	public static native int read(int fd, byte[] buf);

	/**
	 * Appel système write
	 * 
	 * @param fd
	 * @param buf
	 * @return
	 */
	public static native int write(int fd, byte[] buf);

	/**
	 * Appel système poll
	 * 
	 * @param fds
	 * @param timeout
	 * @return
	 */
	public static native int poll(pollfd[] fds, int timeout);

	/**
	 * Appel système lseek
	 * 
	 * @param fd
	 * @param offset
	 * @param whence
	 * @return
	 */
	public static native int lseek(int fd, int offset, int whence);

	/**
	 * Obtention du message d'erreur associé
	 * 
	 * @param errnum
	 * @return
	 */
	public static native String strerror(int errnum);
}
