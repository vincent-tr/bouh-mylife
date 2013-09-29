package mylife.home.net.structure;

/**
 * Membre de classe
 * @author pumbawoman
 *
 */
public class NetMember {

	private final int index;
	private final String name;
	
	/**
	 * Constructeur avec initialisation des données
	 * @param index
	 * @param name
	 */
	public NetMember(int index, String name) {
		this.index = index;
		this.name = name;
	}

	/**
	 * Index du membre
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Nom du membre
	 * @return
	 */
	public String getName() {
		return name;
	}
}
