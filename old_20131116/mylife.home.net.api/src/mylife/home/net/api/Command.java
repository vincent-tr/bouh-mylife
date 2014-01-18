package mylife.home.net.api;

/**
 * Commande envoy�e � un composant
 * @author pumbawoman
 *
 */
public class Command {

	private final String verb;
	private final Tokenizer args;
	private String returnMessage;
	
	/**
	 * Construction de la commande
	 * @param verb
	 * @param args
	 */
	public Command(String verb, Tokenizer args) {
		this.verb = verb;
		this.args = args;
	}
	
	/**
	 * Obtention de l'ordre
	 * @return
	 */
	public String getVerb() {
		return verb;
	}
	
	/**
	 * Obtention des arguments de la commande
	 * @return
	 */
	public Tokenizer getArguments() {
		return args;
	}
	
	/**
	 * D�finition du message du retour de l'ex�cution de la commande
	 * @param value
	 */
	public void setReturnMessage(String value) {
		this.returnMessage = value;
	}

	/**
	 * Obtention du message du retour de l'ex�cution de la commande
	 * @return
	 */
	public String getReturnMessage() {
		return this.returnMessage;
	}
}
