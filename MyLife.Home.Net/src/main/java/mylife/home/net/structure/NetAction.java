package mylife.home.net.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Action
 * @author pumbawoman
 *
 */
public class NetAction extends NetMember {

	private final List<NetType> arguments;
	
	/**
	 * Constructeur avec initialisation des données
	 * @param index
	 * @param name
	 * @param args
	 */
	public NetAction(int index, String name, Iterable<NetType> args) {
		super(index, name);
		ArrayList<NetType> list = new ArrayList<NetType>();
		for(NetType arg : args)
			list.add(arg);
		this.arguments = Collections.unmodifiableList(list);
	}
	
	/**
	 * Constructeur avec initialisation des données
	 * @param index
	 * @param name
	 * @param args
	 */
	public NetAction(int index, String name, NetType ... args) {
		this(index, name, Arrays.asList(args));
	}

	public List<NetType> getArguments() {
		return arguments;
	}
}
