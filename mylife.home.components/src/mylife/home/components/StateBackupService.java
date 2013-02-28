package mylife.home.components;

/**
 * Inteface d'un service de gestion des backup d'�tat des composants
 * @author pumbawoman
 *
 */
public interface StateBackupService {
	
	/**
	 * Obtention de l'�tat d'un composant
	 * @param pid
	 * @return
	 */
	public String getState(String pid);
	
	/**
	 * D�finition de l'�tat d'un composant
	 * @param pid
	 * @param state
	 */
	public void setState(String pid, String state);
}
