package org.mylife.home.net.irc;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestion d'un service de connexion avec reconnexion auto
 * 
 * @author pumbawoman
 * 
 */
public class AutoConnectionService extends Thread implements ConnectionService {

	/**
	 * Logger
	 */
	private final static Logger log = Logger
			.getLogger(AutoConnectionService.class.getName());

	private final static int RETRY_DELAY = 15000;

	/**
	 * Obtention de la fabrique associée
	 * 
	 * @return
	 */
	public static ConnectionServiceFactory getFactory() {
		return new ConnectionServiceFactory() {
			@Override
			public ConnectionService create() {
				return new AutoConnectionService();
			}
		};
	}

	private IRCNetConnection owner;

	private Socket socket;

	@Override
	public void initialize(IRCNetConnection owner) {
		this.owner = owner;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			try {

				if (!owner.isConnected()) {

					// Tentative de connexion puis attente si manqué
					Socket s = tryConnect();
					if (s == null)
						Thread.sleep(RETRY_DELAY);
					else
						socket = s;

				} else {

					// Nous sommes connectés, lecture d'une ligne
					try {
						String line = owner.serviceGetReader().readLine();
						if (line != null)
							owner.serviceLineRead(line);
						else
							close();
					} catch (IOException ex) {
						log.log(Level.WARNING, "Connection error", ex);
						close();
					}
				}

			} catch (InterruptedException e) {
				return;
			}
		}
	}

	/**
	 * Tentative de connexion
	 */
	private Socket tryConnect() {
		Socket s = null;
		try {

			s = new Socket(owner.getHost(), owner.getPort());
			owner.serviceConnected(s);
			return s;

		} catch (IOException e) {
			log.log(Level.WARNING, "Connection failed", e);
			safeClose(s);
			return null;
		}
	}

	@Override
	public void startService() {
		setDaemon(true);
		setName(this.toString());
		start();
	}

	@Override
	public void stopService() {
		if (!isInterrupted())
			interrupt();

		close();
	}

	private void safeClose(Socket s) {
		try {
			if (s != null)
				s.close();
		} catch (Exception exc) {
			log.log(Level.WARNING, "Error closing socket", exc);
		}
	}

	private void close() {
		safeClose(socket);
		owner.serviceClosed();
		socket = null;
	}
}
