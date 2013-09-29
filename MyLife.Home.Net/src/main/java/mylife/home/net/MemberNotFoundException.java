package mylife.home.net;

public class MemberNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7029682794332285378L;

	public MemberNotFoundException() {
		super("Action not found");
	}
}
