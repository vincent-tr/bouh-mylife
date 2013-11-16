package mylife.home.irc.stream;

/**
 * Exception correspondant à une erreur de parsing du message
 * @author pumbawoman
 *
 */
public class MessageParsingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8463037008631741591L;
	
	private static final String DEFAULT_MESSAGE = "Error parsing message"; 

    public MessageParsingException() {
        this(DEFAULT_MESSAGE);
    }

    public MessageParsingException(String message) {
        super(message);
    }

    public MessageParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageParsingException(Throwable cause) {
        this(DEFAULT_MESSAGE, cause);
    }
	
}
