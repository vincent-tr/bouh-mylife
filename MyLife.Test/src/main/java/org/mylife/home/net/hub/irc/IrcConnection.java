package org.mylife.home.net.hub.irc;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.net.hub.irc.commands.Command;
import org.mylife.home.net.hub.irc.commands.CommandFactory;
import org.mylife.home.net.hub.irc.commands.CommandUtils;
import org.mylife.home.net.hub.irc.commands.ConnectionClosedCommand;
import org.mylife.home.net.hub.irc.io.IOConnection;
import org.mylife.home.net.hub.irc.protocol.Message;
import org.mylife.home.net.hub.irc.protocol.Numerics;
import org.mylife.home.net.hub.irc.protocol.Parser;
import org.mylife.home.net.hub.irc.structure.Connectable;

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

	private final IrcServer owner;
	private final IOConnection connection;
	private final Parser parser;
	private final IOConnectionHandler connectionHandler;
	private final ParserHandler parserHandler;

	private Connectable structure;

	public IrcConnection(IrcServer owner, SocketChannel socket) throws IOException {
		this.owner = owner;
		this.connectionHandler = new IOConnectionHandler();
		this.parserHandler = new ParserHandler();
		this.connection = new IOConnection(connectionHandler, socket);
		this.parser = new Parser(parserHandler);
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
			// TODO Auto-generated method stub
		}

		@Override
		public void connectionFailed(IOException e) {
			// TODO Auto-generated method stub
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

	/*internal*/ IOConnection getIOConnection() {
		return connection;
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
		owner.removeConnection(this);
		try {
			connection.close();
		} catch (IOException ex) {
			log.log(Level.SEVERE, "Error closing connection", ex);
		}

		ConnectionClosedCommand cmd = CommandFactory.getInstance().getConnectionClosedCommand();
		cmd.invoke(owner, this);
	}

	private void receive(Message message) {
		
		Command cmd = CommandFactory.getInstance().getCommand(message.getCommand());
		if(cmd == null) {
			CommandUtils.replyError(owner, this, Numerics.ERR_UNKNOWNCOMMAND);
			return;
		}
		
		cmd.invoke(owner, this, message);
	}
	
	/**
	 * Exécute le message spécifié comme s'il était arrivé depuis la partie distante
	 * @param message
	 */
	public void execute(Message message) {
		receive(message);
	}
}
