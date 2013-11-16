package mylife.home.irc.server.structure;

import java.util.ArrayList;

/**
 * Collection de modes
 * @author pumbawoman
 *
 */
public class ModeCollection extends ArrayList<Mode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7111317012897328134L;

	/**
	 * Représentation en chaine des modes
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(this.size() + 1 );
		builder.append('+');
		for(Mode m : this) {
			builder.append(m.getMode());
		}
		return builder.toString();
	}

	
}
