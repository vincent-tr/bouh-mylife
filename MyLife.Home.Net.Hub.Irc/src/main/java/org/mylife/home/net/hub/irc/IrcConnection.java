package org.mylife.home.net.hub.irc;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.net.hub.irc.commands.Command;
import org.mylife.home.net.hub.irc.commands.CommandFactory;
import org.mylife.home.net.hub.irc.commands.CommandUtils;
import org.mylife.home.net.hub.irc.commands.ConnectionClosedCommand;
import org.mylife.home.net.hub.irc.commands.ConnectionOpenedCommand;
import org.mylife.home.net.hub.irc.io.IOConnection;
import org.mylife.home.net.hub.irc.protocol.Message;
import org.mylife.home.net.hub.irc.protocol.Numerics;
import org.mylife.home.net.hub.irc.protocol.Parser;
import org.mylife.home.net.hub.irc.structure.Connectable;
import org.mylife.home.net.hub.irc.tasks.PingTask;

/**
 * Représentation d'une connexion IRC
 * 
 * @author pumbawoman
 * 
 */
public class IrcConnection {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(IrcConnection.class
			.getName());

	private IrcServer owner;
	private IOConnection connection;
	private Parser parser;
	private IOConnectionHandler connectionHandler;
	private ParserHandler parserHandler;
	private PingTask pingTask;
	private IrcConnectHandler connectHandler;
	private boolean locallyInitiated;
	private boolean closing = false;

	private Connectable structure;

	@Override
	public String toString() {
		if (locallyInitiated)
			return "IrcConnection:" + owner.getServerName() + " -> "
					+ connection.getRemoteHost();
		else
			return "IrcConnection:" + connection.getRemoteHost() + " -> "
					+ owner.getServerName();
	}

	/* internal */IrcConnection(IrcServer owner, SocketChannel socket)
			throws IOException {
		locallyInitiated = false;
		init(owner);
		this.connection = new IOConnection(connectionHandler, socket);
	}

	/* internal */IrcConnection(IrcServer owner, String address, int port,
			IrcConnectHandler connectHandler) throws IOException {
		locallyInitiated = true;
		init(owner);
		this.connectHandler = connectHandler;
		this.connection = new IOConnection(connectionHandler, address, port);
	}

	private void init(IrcServer owner) {
		this.owner = owner;
		this.connectionHandler = new IOConnectionHandler();
		this.parserHandler = new ParserHandler();
		this.parser = new Parser(parserHandler);
		this.pingTask = new PingTask(this);
	}

	/* internal */void markConnected() {
		ConnectionOpenedCommand cmd = CommandFactory.getInstance()
				.getConnectionOpenedCommand();
		cmd.invoke(owner, this);

		owner.addScheduledTask(pingTask);
	}

	private class IOConnectionHandler implements IOConnection.Handler {

		@Override
		public void read(byte[] data) {
			parser.readData(data);
		}

		@Override
		public void remoteClosed() {
			close();
		}

		@Override
		public void connected() {
			markConnected();
			connectHandler.connected();
		}

		@Override
		public void connectionFailed(IOException e) {
			close();
			connectHandler.connectionFailed(e);
		}
	}

	private class ParserHandler implements Parser.Handler {

		@Override
		public void messageReceived(Message message) {
			receive(message);
		}

		@Override
		public void writeData(byte[] data) throws IOException {
			connection.write(data);
		}

	}

	/* internal */IOConnection getIOConnection() {
		return connection;
	}

	public boolean getLocallyinitiated() {
		return locallyInitiated;
	}

	public Connectable getStructure() {
		return structure;
	}

	public void setStructure(Connectable structure) {
		this.structure = structure;
	}

	public String getRemoteHost() {
		return connection.getRemoteHost();
	}

	public void send(Message message) {
		try {
			parser.writeMessage(message);
		} catch (IOException ex) {
			log.log(Level.SEVERE, "Error sending message, closing connection",
					ex);
			close();
		}
	}

	public void close() {
		if (closing)
			return;
		try {
			// réentrance
			closing = true;
			owner.removeConnection(this);
			owner.removeScheduledTask(pingTask);
			try {
				connection.close();
			} catch (IOException ex) {
				log.log(Level.SEVERE, "Error closing connection", ex);
			}

			ConnectionClosedCommand cmd = CommandFactory.getInstance()
					.getConnectionClosedCommand();
			cmd.invoke(owner, this);
		} finally {
			closing = false;
		}
	}

	private void receive(Message message) {

		Command cmd = CommandFactory.getInstance().getCommand(
				message.getCommand());
		if (cmd == null) {
			CommandUtils.replyError(owner, this, Numerics.ERR_UNKNOWNCOMMAND);
			return;
		}

		cmd.invoke(owner, this, message);
	}

	/**
	 * Exécute le message spécifié comme s'il était arrivé depuis la partie
	 * distante
	 * 
	 * @param message
	 */
	public void execute(Message message) {
		receive(message);
	}

	public void pong(String value) {
		pingTask.pong(value);
	}
}
