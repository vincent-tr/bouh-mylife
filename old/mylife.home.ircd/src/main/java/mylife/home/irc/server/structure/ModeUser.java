package mylife.home.irc.server.structure;

/**
 * Représentation d'un mode avec un paramètre ciblant un utilisateur
 * @author pumbawoman
 *
 */
public class ModeUser extends Mode {

	/**
	 * Utilisateur cible
	 */
	private final User parameter;
	
	/**
	 * 
	 * @param mode
	 * @param parameter
	 */
	public ModeUser(char mode, User parameter) {
		super(mode);
		this.parameter = parameter;
	}

	/**
	 * Utilisateur cible
	 * @return
	 */
	public User getParameter() {
		return parameter;
	}
	
}
