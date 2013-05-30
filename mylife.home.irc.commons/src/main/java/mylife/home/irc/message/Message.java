package mylife.home.irc.message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Message
 * 
 * @author pumbawoman
 * 
 */
public class Message {

	/**
	 * Préfix optionel
	 */
	private final Prefix prefix;

	/**
	 * Commande (ou valeur numérique)
	 */
	private final String command;

	/**
	 * Paramètres
	 */
	private final List<String> parameters;
	
	/**
	 * Constructeur avec données
	 * 
	 * @param prefix
	 * @param command
	 * @param parameters
	 */
	public Message(Prefix prefix, String command, List<String> parameters) {
		this.prefix = prefix;
		this.command = command;
		this.parameters = Collections.unmodifiableList(parameters);
	}
	
	/**
	 * Constructeur avec données
	 * 
	 * @param prefix
	 * @param command
	 * @param parameters
	 */
	public Message(Prefix prefix, String command, String... parameters) {
		this(prefix, command, Arrays.asList(parameters));
	}

	/**
	 * Préfix optionel
	 * 
	 * @return
	 */
	public Prefix getPrefix() {
		return prefix;
	}

	/**
	 * Commande (ou valeur numérique)
	 * 
	 * @return
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Paramètres
	 * 
	 * @return
	 */
	public List<String> getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if(prefix != null) {
			builder.append(':');
			builder.append(prefix.toString());
			builder.append(' ');
		}
		
		builder.append(command);
		
		// tous les parametres sauf le dernier
		for(int i=0; i<parameters.size() -1 ; i++) {
			builder.append(' ');
			builder.append(parameters.get(i));
		}
		
		// le dernier paramètre avec :
		int index = parameters.size() - 1;
		if(index > -1) {
			builder.append(" :");
			builder.append(parameters.get(index));
		}
		
		return builder.toString();
	}
}
