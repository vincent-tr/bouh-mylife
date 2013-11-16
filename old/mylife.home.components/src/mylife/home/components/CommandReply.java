package mylife.home.components;

/**
 * Réponses aux ordres envoyés aux composants
 * @author pumbawoman
 *
 */
public enum CommandReply {

	// Succès
	OK("200", "OK"),
	ALREADY_ON("201", "Already on, no change made"),
	ALREADY_OFF("202", "Already off, no change made"),
	ALREADY_DIMMABLE_VALUE("203", "Value already set, no change made"),
	
	// Erreurs client
	INVALID_DIMMABLE_VALUE("401", "Invalid value ! must be >= 0 and <= 100");
	
	/**
	 * Code identifiant la réponse
	 */
	private final String code;
	
	/**
	 * Teste de la réponse
	 */
	private final String text;
	
	private CommandReply(String code, String text) {
		this.code = code;
		this.text = text;
	}
	
	/**
	 * Code identifiant la réponse
	 * @return
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * Teste de la réponse
	 * @return
	 */
	public String getText() {
		return text;
	}

	/**
	 * Représentation standard en réponse
	 * @return
	 */
	public String getReply() {
		return code + ":" + text;
	}
}
