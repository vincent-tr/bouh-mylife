package mylife.home.components;

/**
 * Inteface d'un service de gestion des backup d'état des composants
 * @author pumbawoman
 *
 */
public interface StateBackupService {
	
	/**
	 * Obtention de l'état d'un composant
	 * @param pid
	 * @return
	 */
	public String getState(String pid);
	
	/**
	 * Définition de l'état d'un composant
	 * @param pid
	 * @param state
	 */
	public void setState(String pid, String state);
}
