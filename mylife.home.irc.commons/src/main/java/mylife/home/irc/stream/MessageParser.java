package mylife.home.irc.stream;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser de message
 * 
 * @author pumbawoman
 * 
 */
class MessageParser {

	// http://jerklib.svn.sourceforge.net/viewvc/jerklib/jerklib/src/jerklib/events/EventToken.java?revision=872&view=markup

	private final String data;
	private String prefix = null;
	private String command = null;
	private List<String> arguments = new ArrayList<String>();
	private int offset = 0;

	public MessageParser(String data) {
		this.data = data;
		parse();
	}

	/**
	 * Parse message
	 */
	private void parse() {
		if (data.length() == 0)
			return;

		// see if message has prefix
		if (data.startsWith(":")) {
			extractPrefix(data);
			incTillChar();
		}

		// get command
		command = data.substring(offset, data.indexOf(" ", offset));
		offset += command.length();

		incTillChar();
		extractArguments();
	}

	/**
	 * Extract arguments from message
	 */
	private void extractArguments() {
		String argument = null;
		for (int i = offset; i < data.length(); i++) {
			if (!Character.isWhitespace(data.charAt(i))) {
				argument += data.charAt(i);

				// if argument.equals(":") then arg is everything till EOL
				if (argument.length() == 1 && argument.equals(":")) {
					argument = data.substring(i + 1);
					arguments.add(argument);
					return;
				}
				offset++;
			} else {
				if (argument.length() > 0) {
					arguments.add(argument);
					argument = null;
				}
				offset++;
			}
		}

		if (argument.length() != 0) {
			arguments.add(argument);
		}
	}

	/**
	 * Increment offset until a non-whitespace char is found
	 */
	private void incTillChar() {
		for (int i = offset; i < data.length(); i++) {
			if (!Character.isWhitespace(data.charAt(i))) {
				return;
			}
			offset++;
		}
	}

	/**
	 * Extract prefix part of messgae , inc offset
	 * 
	 * @param data
	 */
	private void extractPrefix(String data) {
		// set prefix - : is at 0
		prefix = data.substring(1, data.indexOf(" "));

		// increment offset , +1 is for : removed
		offset += prefix.length() + 1;
	}

	/**
	 * Gets hostname from message
	 * 
	 * @return hostname or empty string if hostname could not be parsed
	 */
	public String getHostName() {
		int index = prefix.indexOf('@');
		if (index != -1 && index + 1 < prefix.length()) {
			return prefix.substring(index + 1);
		}
		return null;
	}

	/**
	 * Get username from message
	 * 
	 * @return username or empty string is username could not be parsed.
	 */
	public String getUserName() {
		int sindex = prefix.indexOf('!');
		int eindex = prefix.indexOf("@");
		if (eindex == -1)
			eindex = prefix.length() - 1;
		if (sindex != -1 && sindex + 1 < prefix.length()) {
			return prefix.substring(sindex + 1, eindex);
		}
		return null;
	}

	/**
	 * Get nick from message
	 * 
	 * @return nick or empty string if could not be parsed
	 */
	public String getNick() {
		if (prefix.indexOf("!") != -1) {
			return prefix.substring(0, prefix.indexOf('!'));
		}
		return null;
	}

	/**
	 * Gets message prefix if any
	 * 
	 * @return returns prefix or empty string if no prefix
	 */
	public String prefix() {
		return prefix;
	}

	/**
	 * Gets the command. This will return the same result as numeric() if the
	 * command is a numeric.
	 * 
	 * @return the command
	 */
	public String command() {
		return command;
	}

	/**
	 * Gets list of arguments
	 * 
	 * @return list of arguments
	 */
	public List<String> args() {
		return arguments;
	}
}
