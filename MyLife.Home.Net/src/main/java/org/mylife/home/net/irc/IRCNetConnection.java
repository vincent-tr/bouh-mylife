package org.mylife.home.net.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.IRCUtil;

/**
 * Connexion
 * 
 * @author trumpffv
 * 
 */
public class IRCNetConnection {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(IRCNetConnection.class
			.getName());

	/**
	 * Gestion de la fabrique de connexions
	 */
	private static ConnectionServiceFactory connectionServiceFactory = AutoConnectionService.getFactory();

	/**
	 * Gestion de la fabrique de connexions
	 * 
	 * @return
	 */
	public static ConnectionServiceFactory getConnectionServiceFactory() {
		return connectionServiceFactory;
	}

	/**
	 * Gestion de la fabrique de connexions
	 * 
	 * @param connectionServiceFactory
	 */
	public static void setConnectionServiceFactory(
			ConnectionServiceFactory connectionServiceFactory) {
		IRCNetConnection.connectionServiceFactory = connectionServiceFactory;
	}

	/**
	 * Gestionnaire de connexion courant
	 */
	protected final ConnectionService connectionService;
	
	/**
	 * This is like a UNIX-runlevel. Its value indicates the level of the
	 * <code>IRCConnection</code> object. <code>0</code> means that the object
	 * has not yet been connected, <code>1</code> means that it's connected but
	 * not registered, <code>2</code> means that it's connected and registered
	 * but still waiting to receive the nickname the first time, <code>3</code>
	 * means that it's connected and registered, and <code>-1</code> means that
	 * it was connected but is disconnected. Therefore the defaultvalue is
	 * <code>0</code>.
	 */
	protected byte level = 0;

	/**
	 * The host of the IRC server.
	 */
	protected String host;

	/**
	 * Port to connect
	 */
	protected int port;

	/**
	 * The <code>BufferedReader</code> receives Strings from the IRC server.
	 */
	private volatile BufferedReader in;

	/**
	 * The <code>PrintWriter</code> sends Strings to the IRC server.
	 */
	private PrintWriter out;

	/**
	 * The <code>String</code> contains the name of the character encoding used
	 * to talk to the server. This can be ISO-8859-1 or UTF-8 for example. The
	 * default is ISO-8859-1.
	 */
	protected String encoding = "ISO-8859-1";

	/**
	 * This array contains <code>IRCEventListener</code> objects.
	 */
	private IRCEventListener[] listeners = new IRCEventListener[0];

	/**
	 * This <code>int</code> is the connection's timeout in milliseconds. It's
	 * used in the <code>Socket.setSoTimeout</code> method. The default is
	 * <code>1000 * 60 * 15</code> millis which are 15 minutes.
	 */
	private final static int timeout = 1000 * 60 * 15;

	/**
	 * This <code>boolean</code> stands for enabled (<code>true</code>) or
	 * disabled (<code>false</code>) ColorCodes.<br />
	 * Default is enabled (<code>false</code>).
	 */
	private boolean colorsEnabled = false;

	/**
	 * This <code>boolean</code> stands for enabled or disabled automatic PING?
	 * PONG! support. <br />
	 * It means, that if the server asks with PING for the ping, the PONG is
	 * automatically sent. Default is automatic PONG enabled (<code>true</code>
	 * ).
	 */
	private boolean pongAutomatic = true;

	/**
	 * The password, which is needed to get access to the IRC server.
	 */
	private String pass;

	/**
	 * The user's nickname, which is indispensably to connect.
	 */
	private String nick;

	/**
	 * The user's realname, which is indispensably to connect.
	 */
	private String realname;

	/**
	 * The user's username, which is indispensable to connect.
	 */
	private String username;
	
	/** 
	 * This <code>Socket</code> is a connection to the IRC server. 
	 */
	private Socket socket;

	/**
	 * Constructeur avec données
	 * 
	 * @param host
	 * @param port
	 * @param nick
	 * @param id
	 */
	public IRCNetConnection(String host, int port, String nick, String id) {
		if (host == null || port <= 0)
			throw new IllegalArgumentException(
					"Host and ports may not be null.");
		this.host = host;
		this.port = port;
		this.pass = (pass != null && pass.length() == 0) ? null : pass;
		this.nick = nick;
		this.username = id;
		this.realname = id;
		this.connectionService = connectionServiceFactory.create();
		this.connectionService.initialize(this);
	}

	/**
	 * Quand la connexion est ouverte
	 */
	public synchronized void serviceConnected(Socket s) throws IOException {
		if (s == null)
			throw new SocketException("Socket s is null, not connected");
		socket = s;
		level = 1;
		s.setSoTimeout(timeout);
		in  = new BufferedReader(new InputStreamReader(s.getInputStream(), 
				encoding));
		out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), 
				encoding));
		//start();
		register();
	}

	/**
	 * Quand la connexion est fermée
	 */
	public synchronized void serviceClosed() {
		try {
			if (out != null)
				out.close();
		} catch (Exception exc) {
			log.log(Level.WARNING, "Error closing out", exc);
		}
		try {
			if (in != null)
				in.close();
		} catch (Exception exc) {
			log.log(Level.WARNING, "Error closing in", exc);
		}
		if (this.level > 0) {
			this.level = 0;
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onDisconnected();
		}
		in = null;
		out = null;
		socket = null;
	}
	
	public BufferedReader serviceGetReader() {
		return in;
	}

	/**
	 * Démarrage
	 */
	public void start() {
		connectionService.startService();
	}
	
	/**
	 * Arrêt
	 */
	public void stop() {
		connectionService.stopService();
	}
	
	// ------------------------------

	/**
	 * Sends a String to the server. You should use this method only, if you
	 * must do it. For most purposes, there are <code>do*</code> methods (like
	 * <code>doJoin</code>). A carriage return line feed (<code>\r\n</code>) is
	 * appended automatically.
	 * 
	 * @param line
	 *            The line which should be send to the server without the
	 *            trailing carriage return line feed (<code>\r\n</code>).
	 */
	public void send(String line) {
		try {
			out.write(line + "\r\n");
			out.flush();
			if (level == 1) { // not registered
				IRCParser p = new IRCParser(line);
				if (p.getCommand().equalsIgnoreCase("NICK"))
					nick = p.getParameter(1).trim();
			}
		} catch (Exception exc) {
			log.log(Level.SEVERE, "Error sending message", exc);
		}
	}

	// ------------------------------

	/**
	 * Registers the connection with the IRC server. <br />
	 * In fact, it sends a password (if set, else nothing), the nickname and the
	 * user, the realname and the host which we're connecting to.<br />
	 * The action synchronizes <code>code> so that no important messages 
	 * (like the first PING) come in before this registration is finished.<br />
	 * The <code>USER</code> command's format is:<br />
	 * <code>
	 * &lt;username&gt; &lt;localhost&gt; &lt;irchost&gt; &lt;realname&gt;
	 * </code>
	 */
	private void register() {
		if (pass != null)
			send("PASS " + pass);
		send("NICK " + nick);
		send("USER " + username + " " + socket.getLocalAddress() + " " + host
				+ " :" + realname);
	}

	// ------------------------------

	/**
	 * Just parses a String given as the only argument with the help of the
	 * <code>IRCParser</code> class. Then it controls the command and fires
	 * events through the <code>IRCEventListener</code>.<br />
	 * 
	 * @param line
	 *            The line which is sent from the server.
	 */
	public synchronized void serviceLineRead(String line) {
		IRCParser p;
		try {
			p = new IRCParser(line, colorsEnabled);
		} catch (Exception exc) {
			return;
		}
		String command = p.getCommand();
		int reply; // 3-digit reply will be parsed in the later if-condition

		if (command.equalsIgnoreCase("PRIVMSG")) { // MESSAGE

			IRCUser user = p.getUser();
			String middle = p.getMiddle();
			String trailing = p.getTrailing();
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onPrivmsg(middle, user, trailing);

		} else if (command.equalsIgnoreCase("MODE")) { // MODE

			String chan = p.getParameter(1);
			if (IRCUtil.isChan(chan)) {
				IRCUser user = p.getUser();
				String param2 = p.getParameter(2);
				String paramsFrom3 = p.getParametersFrom(3);
				for (int i = listeners.length - 1; i >= 0; i--)
					listeners[i].onMode(chan, user, new IRCModeParser(param2,
							paramsFrom3));
			} else {
				IRCUser user = p.getUser();
				String paramsFrom2 = p.getParametersFrom(2);
				for (int i = listeners.length - 1; i >= 0; i--)
					listeners[i].onMode(user, chan, paramsFrom2);
			}

		} else if (command.equalsIgnoreCase("PING")) { // PING

			String ping = p.getTrailing(); // no int cause sometimes it's text
			if (pongAutomatic)
				doPong(ping);
			else
				for (int i = listeners.length - 1; i >= 0; i--)
					listeners[i].onPing(ping);

			if (level == 1) { // not registered
				level = 2; // first PING received -> connection
				for (int i = listeners.length - 1; i >= 0; i--)
					listeners[i].onRegistered();
			}

		} else if (command.equalsIgnoreCase("JOIN")) { // JOIN

			IRCUser user = p.getUser();
			String trailing = p.getTrailing();
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onJoin(trailing, user);

		} else if (command.equalsIgnoreCase("NICK")) { // NICK

			IRCUser user = p.getUser();
			String changingNick = p.getNick();
			String newNick = p.getTrailing();
			if (changingNick.equalsIgnoreCase(nick))
				nick = newNick;
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onNick(user, newNick);

		} else if (command.equalsIgnoreCase("QUIT")) { // QUIT

			IRCUser user = p.getUser();
			String trailing = p.getTrailing();
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onQuit(user, trailing);

		} else if (command.equalsIgnoreCase("PART")) { // PART

			IRCUser user = p.getUser();
			String chan = p.getParameter(1);
			String msg = p.getParameterCount() > 1 ? p.getTrailing() : "";
			// not logic: "PART :#zentrum" is without msg,
			// "PART #zentrum :cjo all"
			// is with msg. so we cannot use getMiddle and getTrailing :-/
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onPart(chan, user, msg);

		} else if (command.equalsIgnoreCase("NOTICE")) { // NOTICE

			IRCUser user = p.getUser();
			String middle = p.getMiddle();
			String trailing = p.getTrailing();
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onNotice(middle, user, trailing);

		} else if ((reply = IRCUtil.parseInt(command)) >= 1 && reply < 400) { // RPL

			String potNick = p.getParameter(1);
			if ((level == 1 || level == 2)
					&& nick.length() > potNick.length()
					&& nick.substring(0, potNick.length()).equalsIgnoreCase(
							potNick)) {
				nick = potNick;
				if (level == 2)
					level = 3;
			}

			if (level == 1) { // not registered
				level = 2; // if first PING wasn't received, we're
				for (int i = listeners.length - 1; i >= 0; i--)
					listeners[i].onRegistered(); // connected now for sure
			}

			String middle = p.getMiddle();
			String trailing = p.getTrailing();
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onReply(reply, middle, trailing);

		} else if (reply >= 400 && reply < 600) { // ERROR

			String trailing = p.getTrailing();
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onError(reply, trailing);

		} else if (command.equalsIgnoreCase("KICK")) { // KICK

			IRCUser user = p.getUser();
			String param1 = p.getParameter(1);
			String param2 = p.getParameter(2);
			String msg = (p.getParameterCount() > 2) ? p.getTrailing() : "";
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onKick(param1, user, param2, msg);

		} else if (command.equalsIgnoreCase("INVITE")) { // INVITE

			IRCUser user = p.getUser();
			String middle = p.getMiddle();
			String trailing = p.getTrailing();
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onInvite(trailing, user, middle);

		} else if (command.equalsIgnoreCase("TOPIC")) { // TOPIC

			IRCUser user = p.getUser();
			String middle = p.getMiddle();
			String trailing = p.getTrailing();
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onTopic(middle, user, trailing);

		} else if (command.equalsIgnoreCase("ERROR")) { // ERROR

			String trailing = p.getTrailing();
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].onError(trailing);

		} else { // OTHER

			String prefix = p.getPrefix();
			String middle = p.getMiddle();
			String trailing = p.getTrailing();
			for (int i = listeners.length - 1; i >= 0; i--)
				listeners[i].unknown(prefix, command, middle, trailing);

		}
	}

	// ------------------------------
	// ------------------------------

	/**
	 * Adds a new {@link org.schwering.irc.lib.IRCEventListener} which listens
	 * for actions coming from the IRC server.
	 * 
	 * @param l
	 *            An instance of the
	 *            {@link org.schwering.irc.lib.IRCEventListener} interface.
	 * @throws IllegalArgumentException
	 *             If <code>listener</code> is <code>null</code>.
	 */
	public synchronized void addIRCEventListener(IRCEventListener l) {
		if (l == null)
			throw new IllegalArgumentException("Listener is null.");
		int len = listeners.length;
		IRCEventListener[] oldListeners = listeners;
		listeners = new IRCEventListener[len + 1];
		System.arraycopy(oldListeners, 0, listeners, 0, len);
		listeners[len] = l;
	}

	// ------------------------------

	/**
	 * Removes the first occurence of the given
	 * {@link org.schwering.irc.lib.IRCEventListener} from the listener-vector.
	 * 
	 * @param l
	 *            An instance of the
	 *            {@link org.schwering.irc.lib.IRCEventListener} interface.
	 * @return <code>true</code> if the listener was successfully removed;
	 *         <code>false</code> if it was not found.
	 */
	public synchronized boolean removeIRCEventListener(IRCEventListener l) {
		if (l == null)
			return false;
		int index = -1;
		for (int i = 0; i < listeners.length; i++)
			if (listeners[i].equals(l)) {
				index = i;
				break;
			}
		if (index == -1)
			return false;
		listeners[index] = null;
		int len = listeners.length - 1;
		IRCEventListener[] newListeners = new IRCEventListener[len];
		for (int i = 0, j = 0; i < len; j++)
			if (listeners[j] != null)
				newListeners[i++] = listeners[j];
		listeners = newListeners;
		return true;
	}

	// ------------------------------

	/**
	 * Enables or disables the mIRC colorcodes.
	 * 
	 * @param colors
	 *            <code>true</code> to enable, <code>false</code> to disable
	 *            colors.
	 */
	public void setColors(boolean colors) {
		colorsEnabled = colors;
	}

	// ------------------------------

	/**
	 * Enables or disables the automatic PING? PONG! support.
	 * 
	 * @param pong
	 *            <code>true</code> to enable automatic <code>PONG</code> reply,
	 *            <code>false</code> makes the class fire <code>onPing</code>
	 *            events.
	 */
	public void setPong(boolean pong) {
		pongAutomatic = pong;
	}

	// ------------------------------

	/**
	 * Changes the character encoding used to talk to the server. This can be
	 * ISO-8859-1 or UTF-8 for example. This property must be set before a call
	 * to the <code>connect()</code> method.
	 * 
	 * @param encoding
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	// ------------------------------

	/**
	 * Tells whether there's a connection to the IRC network or not. <br />
	 * If <code>connect</code> wasn't called yet, it returns <code>false</code>.
	 * 
	 * @return The status of the connection; <code>true</code> if it's
	 *         connected.
	 * @see #connect()
	 * @see #doQuit()
	 * @see #doQuit(String)
	 * @see #close()
	 */
	public boolean isConnected() {
		return level >= 1;
	}

	// ------------------------------

	/**
	 * Returns the nickname of this instance.
	 * 
	 * @return The nickname.
	 */
	public String getNick() {
		return nick;
	}

	// ------------------------------

	/**
	 * Returns the realname of this instance.
	 * 
	 * @return The realname.
	 */
	public String getRealname() {
		return realname;
	}

	// ------------------------------

	/**
	 * Returns the username of this instance.
	 * 
	 * @return The username.
	 */
	public String getUsername() {
		return username;
	}

	// ------------------------------

	/**
	 * Returns the server of this instance.
	 * 
	 * @return The server's hostname.
	 */
	public String getHost() {
		return host;
	}

	// ------------------------------

	/**
	 * Returns the password of this instance. If no password is set,
	 * <code>null</code> is returned.
	 * 
	 * @return The password. If no password is set, <code>null</code> is
	 *         returned.
	 */
	public String getPassword() {
		return pass;
	}

	// ------------------------------

	/**
	 * Returns all ports to which the <code>IRCConnection</code> is going to try
	 * or has tried to connect to.
	 * 
	 * @return The ports in an <code>int[]</code> array.
	 */
	public int getPort() {
		return port;
	}

	// ------------------------------

	/**
	 * Indicates whether colors are stripped out or not.
	 * 
	 * @return <code>true</code> if colors are disabled.
	 */
	public boolean getColors() {
		return colorsEnabled;
	}

	// ------------------------------

	/**
	 * Indicates whether automatic PING? PONG! is enabled or not.
	 * 
	 * @return <code>true</code> if PING? PONG! is done automatically.
	 */
	public boolean getPong() {
		return pongAutomatic;
	}

	// ------------------------------

	/**
	 * Returns the encoding of the socket.
	 * 
	 * @return The socket's encoding.
	 */
	public String getEncoding() {
		return encoding;
	}

	// ------------------------------

	/**
	 * Generates a <code>String</code> with some information about the instance
	 * of <code>IRCConnection</code>. Its format is: <code>
	 * classname[host,portMin,portMax,username,nick,realname,pass,connected]
	 * </code>.
	 * 
	 * @return A <code>String</code> with information about the instance.
	 */
	public String toString() {
		return getClass().getName() + "[" + host + "," + getPort() + ","
				+ username + "," + nick + "," + realname + "," + pass + ","
				+ isConnected() + "]";
	}

	// ------------------------------

	/**
	 * Removes away message.
	 */
	public void doAway() {
		send("AWAY");
	}

	// ------------------------------

	/**
	 * Sets away message.
	 * 
	 * @param msg
	 *            The away message.
	 */
	public void doAway(String msg) {
		send("AWAY :" + msg);
	}

	// ------------------------------

	/**
	 * Invites a user to a channel.
	 * 
	 * @param nick
	 *            The nickname of the user who should be invited.
	 * @param chan
	 *            The channel the user should be invited to.
	 */
	public void doInvite(String nick, String chan) {
		send("INVITE " + nick + " " + chan);
	}

	// ------------------------------

	/**
	 * Checks if one or more nicks are used on the server.
	 * 
	 * @param nick
	 *            The nickname of the user we search for.
	 */
	public void doIson(String nick) {
		send("ISON " + nick);
	}

	// ------------------------------

	/**
	 * Joins a channel without a key.
	 * 
	 * @param chan
	 *            The channel which is to join.
	 */
	public void doJoin(String chan) {
		send("JOIN " + chan);
	}

	// ------------------------------

	/**
	 * Joins a channel with a key.
	 * 
	 * @param chan
	 *            The channel which is to join.
	 * @param key
	 *            The key of the channel.
	 */
	public void doJoin(String chan, String key) {
		send("JOIN " + chan + " " + key);
	}

	// ------------------------------

	/**
	 * Kicks a user from a channel.
	 * 
	 * @param chan
	 *            The channel somebody should be kicked from.
	 * @param nick
	 *            The nickname of the user who should be kicked.
	 */
	public void doKick(String chan, String nick) {
		send("KICK " + chan + " " + nick);
	}

	// ------------------------------

	/**
	 * Kicks a user from a channel with a comment.
	 * 
	 * @param chan
	 *            The channel somebody should be kicked from.
	 * @param nick
	 *            The nickname of the user who should be kicked.
	 * @param msg
	 *            The optional kickmessage.
	 */
	public void doKick(String chan, String nick, String msg) {
		send("KICK " + chan + " " + nick + " :" + msg);
	}

	// ------------------------------

	/**
	 * Lists all channels with their topic and status.
	 */
	public void doList() {
		send("LIST");
	}

	// ------------------------------

	/**
	 * Lists channel(s) with their topic and status.
	 * 
	 * @param chan
	 *            The channel the <code>LIST</code> refers to.
	 */
	public void doList(String chan) {
		send("LIST " + chan);
	}

	// ------------------------------

	/**
	 * Lists all visible users.
	 */
	public void doNames() {
		send("NAMES");
	}

	// ------------------------------

	/**
	 * Lists all visible users of (a) channel(s).
	 * 
	 * @param chan
	 *            The channel the <code>NAMES</code> command is refering to.
	 */
	public void doNames(String chan) {
		send("NAMES " + chan);
	}

	// ------------------------------

	/**
	 * Sends a message to a person or a channel.
	 * 
	 * @param target
	 *            The nickname or channel the message should be sent to.
	 * @param msg
	 *            The message which should be transmitted.
	 */
	public void doPrivmsg(String target, String msg) {
		send("PRIVMSG " + target + " :" + msg);
	}

	// ------------------------------

	/**
	 * Requests a Reply 324 for the modes of a given channel.
	 * 
	 * @param chan
	 *            The channel the <code>MODE</code> request is refering to.
	 */
	public void doMode(String chan) {
		send("MODE " + chan);
	}

	// ------------------------------

	/**
	 * Sends a mode to the server. <br />
	 * The first argument is a nickname (user-mode) or a channel (channel-mode).
	 * <code>String mode</code> must contain the operators (+/-), the modes
	 * (o/v/i/k/l/p/s/w) and the possibly values (nicks/banmask/limit/key).
	 * 
	 * @param target
	 *            The nickname or channel of the user whose modes will be
	 *            changed.
	 * @param mode
	 *            The new modes.
	 */
	public void doMode(String target, String mode) {
		send("MODE " + target + " " + mode);
	}

	// ------------------------------

	/**
	 * Changes the nickname.
	 * 
	 * @param nick
	 *            The new nickname.
	 */
	public void doNick(String nick) {
		send("NICK " + nick);
	}

	// ------------------------------

	/**
	 * Notices a message to a person or a channel.
	 * 
	 * @param target
	 *            The nickname or channel (group) the message should be sent to.
	 * @param msg
	 *            The message which should be transmitted.
	 */
	public void doNotice(String target, String msg) {
		send("NOTICE " + target + " :" + msg);
	}

	// ------------------------------

	/**
	 * Parts from a given channel.
	 * 
	 * @param chan
	 *            The channel you want to part from.
	 */
	public void doPart(String chan) {
		send("PART " + chan);
	}

	// ------------------------------

	/**
	 * Parts from a given channel with a given parg-msg.
	 * 
	 * @param chan
	 *            The channel you want to part from.
	 * @param msg
	 *            The optional partmessage.
	 */
	public void doPart(String chan, String msg) {
		send("PART " + chan + " :" + msg);
	}

	// ------------------------------

	/**
	 * Quits from the IRC server with a quit-msg.
	 * 
	 * @param ping
	 *            The ping which was received in <code>onPing</code>. It's a
	 *            <code>String</code>, because sometimes on some networks the
	 *            server-hostname (for example splatterworld.quakenet.org) is
	 *            given as parameter which would throw an Exception if we gave
	 *            the ping as long.
	 */
	public void doPong(String ping) {
		send("PONG :" + ping);
	}

	// ------------------------------

	/**
	 * Quits from the IRC server. Calls the <code>disconnect</code>-method which
	 * does the work actually.
	 * 
	 * @see #isConnected()
	 * @see #connect()
	 * @see #doQuit(String)
	 * @see #close()
	 */
	public void doQuit() {
		send("QUIT");
	}

	// ------------------------------

	/**
	 * Quits from the IRC server with a quit-msg. Calls the
	 * <code>disconnect</code>-method which does the work actually.
	 * 
	 * @param msg
	 *            The optional quitmessage.
	 * @see #isConnected()
	 * @see #connect()
	 * @see #doQuit()
	 * @see #close()
	 */
	public void doQuit(String msg) {
		send("QUIT :" + msg);
	}

	// ------------------------------

	/**
	 * Requests the topic of a chan. The topic is given in a numeric reply.
	 * 
	 * @param chan
	 *            The channel which topic should be requested.
	 */
	public void doTopic(String chan) {
		send("TOPIC " + chan);
	}

	// ------------------------------

	/**
	 * Changes the topic of a chan.
	 * 
	 * @param chan
	 *            The channel which topic is changed.
	 * @param topic
	 *            The new topic.
	 */
	public void doTopic(String chan, String topic) {
		send("TOPIC " + chan + " :" + topic);
	}

	// ------------------------------

	/**
	 * Requests information about users matching the given criteric, for example
	 * a channel they are on.
	 * 
	 * @param criteric
	 *            The criterics of the <code>WHO</code> query.
	 */
	public void doWho(String criteric) {
		send("WHO " + criteric);
	}

	// ------------------------------

	/**
	 * Requires information about an existing user.
	 * 
	 * @param nick
	 *            The nickname of the user the query is refering to.
	 */
	public void doWhois(String nick) {
		send("WHOIS " + nick);
	}

	// ------------------------------

	/**
	 * Requires host-information about a user, who is not connected anymore.
	 * 
	 * @param nick
	 *            The nickname of the user the query is refering to.
	 */
	public void doWhowas(String nick) {
		send("WHOWAS " + nick);
	}

	// ------------------------------

	/**
	 * Requires host-information about up to 5 users which must be listed and
	 * divided by spaces.
	 * 
	 * @param nick
	 *            The nickname of the user the query is refering to.
	 */
	public void doUserhost(String nick) {
		send("USERHOST " + nick);
	}
}
