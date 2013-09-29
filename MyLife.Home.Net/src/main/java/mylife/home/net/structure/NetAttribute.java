package mylife.home.net.structure;


/**
 * Attribut
 * @author pumbawoman
 *
 */
public class NetAttribute extends NetMember {

	private final NetType type;
	
	/**
	 * Constructeur avec initialisation des donn�es
	 * @param index
	 * @param name
	 * @param type
	 */
	public NetAttribute(int index, String name, NetType type) {
		super(index, name);
		this.type = type;
	}

	/**
	 * Type de l'argument
	 * @return
	 */
	public NetType getType() {
		return type;
	}
}
