package mylife.home.net.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Représentation d'une classe
 * 
 * @author pumbawoman
 * 
 */
public class NetClass {

	private final List<NetMember> members;

	/**
	 * Constructeur avec membres
	 * @param members
	 */
	public NetClass(Iterable<NetMember> members) {

		ArrayList<NetMember> list = new ArrayList<NetMember>();
		for (NetMember member : members)
			list.add(member);

		Collections.sort(list, new Comparator<NetMember>() {

			@Override
			public int compare(NetMember arg0, NetMember arg1) {
				return Integer.compare(arg0.getIndex(), arg1.getIndex());
			}

		});
		this.members = Collections.unmodifiableList(list);
	}

	/**
	 * Constructeur avec membres
	 * @param members
	 */
	public NetClass(NetMember ... members) {
		this(Arrays.asList(members));
	}
	
	public List<NetMember> getMembers() {
		return members;
	}
}
