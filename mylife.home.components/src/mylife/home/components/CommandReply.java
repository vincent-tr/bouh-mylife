package mylife.home.components;

/**
 * R�ponses aux ordres envoy�s aux composants
 * @author pumbawoman
 *
 */
public enum CommandReply {

	// Succ�s
	OK("200", "OK"),
	ALREADY_ON("201", "Already on, no change made"),
	ALREADY_OFF("202", "Already off, no change made"),
	ALREADY_DIMMABLE_VALUE("203", "Value already set, no change made"),
	
	// Erreurs client
	INVALID_DIMMABLE_VALUE("401", "Invalid value ! must be >= 0 and <= 100");
	
	/**
	 * Code identifiant la r�ponse
	 */
	private final String code;
	
	/**
	 * Teste de la r�ponse
	 */
	private final String text;
	
	private CommandReply(String code, String text) {
		this.code = code;
		this.text = text;
	}
	
	/**
	 * Code identifiant la r�ponse
	 * @return
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * Teste de la r�ponse
	 * @return
	 */
	public String getText() {
		return text;
	}

	/**
	 * Repr�sentation standard en r�ponse
	 * @return
	 */
	public String getReply() {
		return code + ":" + text;
	}
}
