package org.mylife.home.net.hub.irc.protocol;

/**
 * Message
 * 
 * @author pumbawoman
 * 
 */
public class Message {
	protected final String from;
	protected final String command;
	protected String[] params = new String[3];
	protected int paramCount = 0;
	protected boolean hasLast = false;

	/**
	 * Creates a message.
	 * 
	 * @param from
	 *            can be null
	 */
	public Message(String from, String command) {
		if (!Util.isCommandIdentifier(command))
			throw new IllegalArgumentException("Invalid command name");
		this.from = from;
		this.command = command;
	}

	public Message(String command) {
		this(null, command);
	}

	/**
	 * Returns the sender of this message.
	 */
	public String getSender() {
		return from;
	}

	/**
	 * Returns the command of this message.
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Indique si le message est une réponse numérique
	 * @return
	 */
	public boolean isReply() {
		return Character.isDigit(command.charAt(0));
	}

	private void addParam(String param) {
		if (paramCount == params.length) {
			int newLength = 2 * (paramCount + 1);
			String[] old = params;
			params = new String[newLength];
			System.arraycopy(old, 0, params, 0, paramCount);
		}
		params[paramCount++] = param;
	}

	/**
	 * Appends a parameter.
	 */
	public Message appendParameter(String param) {
		if (hasLast)
			throw new IllegalStateException(
					"The last parameter has already been appended");
		if (!Util.isParameter(param))
			throw new IllegalArgumentException(
					"Use appendLastParameter() instead");
		addParam(param);
		return this;
	}

	/**
	 * Explicitly appends the last parameter.
	 */
	public Message appendLastParameter(String param) {
		if (hasLast)
			throw new IllegalStateException(
					"The last parameter has already been appended");
		addParam(param);
		hasLast = true;
		return this;
	}

	public String getParameter(int n) {
		return params[n];
	}

	public int getParameterCount() {
		return paramCount;
	}

	/**
	 * Returns true if there is an explicit last parameter.
	 */
	public boolean hasLastParameter() {
		return hasLast;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append('[').append(from);
		buf.append(", ").append(command).append(", [");
		int lastIndex = paramCount - 1;
		for (int i = 0; i < lastIndex; i++)
			buf.append(params[i]).append(", ");
		buf.append(params[lastIndex]).append("], ");
		buf.append(hasLast).append(']');
		return buf.toString();
	}
}
