package org.mylife.home.net.hub.irc.protocol;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Parser
 * 
 * @author pumbawoman
 * 
 */
public class Parser {

	public interface Handler {
		public void messageReceived(Message message);

		public void writeData(byte[] data) throws IOException;
	}

	private final Handler handler;
	private String buffer = "";

	public Parser(Handler handler) {
		this.handler = handler;
	}

	public void readData(byte[] data) {
		String sdata = new String(data, Charset.forName(Constants.CHARSET));
		buffer += sdata;

		int offset;
		while ((offset = indexOf(buffer,
				Constants.MESSAGE_TERMINATOR.toCharArray())) >= 0) {
			String rawmsg = buffer.substring(0, offset);
			buffer = buffer.substring(offset + 1);

			// si on recoit des \r\n ca va créer des lignes vides
			if (rawmsg.length() > 0)
				parseRawMessage(rawmsg);
		}
	}

	/**
	 * 1er index possible des chars
	 * 
	 * @param data
	 * @param chars
	 * @return
	 */
	private int indexOf(String data, char... chars) {
		int min = Integer.MAX_VALUE;
		for (char c : chars) {
			int offset = data.indexOf(c);
			if (offset == -1)
				continue;

			if (min > offset)
				min = offset;
		}

		if (min == Integer.MAX_VALUE)
			min = -1;
		return min;
	}

	private void parseRawMessage(String rawmsg) {
		rawmsg = rawmsg.trim();
		// max length per RFC
		// if (rawmsg.length() > Constants.MAX_MESSAGE_LENGTH)
		// rawmsg = rawmsg.substring(0, Constants.MAX_MESSAGE_LENGTH);

		int startPos = 0;

		// parse prefix
		String from = null;
		if (rawmsg.charAt(0) == ':') {
			int endPos = rawmsg.indexOf(' ', 2);
			from = rawmsg.substring(1, endPos);
			startPos = endPos + 1;
		}
		Message message = parseMessage(from, rawmsg, startPos);

		handler.messageReceived(message);
	}

	private Message parseMessage(String from, String str, int startPos) {
		// parse command
		int endPos = str.indexOf(' ', startPos);
		if (endPos == -1) {
			// no parameters
			String command = str.substring(startPos);
			return new Message(from, command);
		}

		String command = str.substring(startPos, endPos);
		Message message = new Message(from, command);

		// parse parameters
		int trailingPos = str.indexOf(" :", endPos);
		if (trailingPos == -1)
			trailingPos = str.length();
		while (endPos != -1 && endPos < trailingPos) {
			startPos = endPos + 1;
			endPos = str.indexOf(' ', startPos);
			if (endPos != -1 && endPos - startPos > 0)
				message.appendParameter(str.substring(startPos, endPos));
		}
		if (endPos == -1) {
			message.appendParameter(str.substring(startPos));
		} else {
			message.appendLastParameter(str.substring(trailingPos + 2));
		}
		return message;
	}

	public void writeMessage(Message msg) throws IOException {
		StringBuffer buf = new StringBuffer();
		// append prefix
		String sender = msg.getSender();
		if (sender != null)
			buf.append(':').append(sender).append(' ');

		// append command
		buf.append(msg.getCommand());

		// append parameters
		final int paramCount = msg.getParameterCount();
		if (paramCount > 0) {
			final int lastParamIndex = paramCount - 1;
			for (int i = 0; i < lastParamIndex; i++)
				buf.append(' ').append(msg.getParameter(i));
			if (msg.hasLastParameter())
				buf.append(" :").append(msg.getParameter(lastParamIndex));
			else
				buf.append(' ').append(msg.getParameter(lastParamIndex));
		}

		buf.append(Constants.MESSAGE_TERMINATOR);
		byte[] data = buf.toString().getBytes(Constants.CHARSET);
		handler.writeData(data);
	}
}
