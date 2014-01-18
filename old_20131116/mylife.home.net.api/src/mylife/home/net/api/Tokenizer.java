package mylife.home.net.api;

import java.util.StringTokenizer;
import java.util.UUID;

/**
 * Gestion du split de la chaine d'arguments
 * @author pumbawoman
 *
 */
public class Tokenizer {

	private final String source;
	private final StringTokenizer tokenizer;
	
	/**
	 * Constructeur avec source
	 * @param source
	 */
	public Tokenizer(String source) {
		this.source = source;
		this.tokenizer = new StringTokenizer(source, " ");
	}
	
	/**
	 * Obtention de la source
	 * @return
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * Peut-on appeler next ?
	 * @return
	 */
	public boolean hasNext() {
		return tokenizer.hasMoreTokens();
	}
	
	/**
	 * Obtention du token suivant
	 * @return
	 */
	public String next() {
		return tokenizer.nextToken();
	}
	
	/**
	 * Obtention de tout le reste des arguments en un seul bloc
	 * @return
	 */
	public String remaining() {
		return tokenizer.nextToken(UUID.randomUUID().toString());
	}
}
