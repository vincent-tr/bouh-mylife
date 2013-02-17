package mylife.home.net.api;

/**
 * Commande envoyée à un composant
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
	 * Définition du message du retour de l'exécution de la commande
	 * @param value
	 */
	public void setReturnMessage(String value) {
		this.returnMessage = value;
	}

	/**
	 * Obtention du message du retour de l'exécution de la commande
	 * @return
	 */
	public String getReturnMessage() {
		return this.returnMessage;
	}
}
