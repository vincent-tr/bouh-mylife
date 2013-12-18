package org.mylife.home.core.plugins.enhanced;

import org.mylife.home.net.NetObject;
import org.mylife.home.net.structure.NetMember;

/**
 * Gestion d'un membre
 * 
 * @author pumbawoman
 * 
 */
abstract class MemberWrapper {

	/**
	 * Création du membre
	 * 
	 * @return
	 */
	public abstract NetMember createMember();

	/**
	 * Binding sur le NetObject
	 * 
	 * @param netObject
	 */
	public abstract void bind(NetObject netObject);

	/**
	 * Suppression du binding
	 * 
	 * @param netObject
	 */
	public abstract void unbind(NetObject netObject);
	
	/**
	 * Conversion d'une valeur locale en valeur net
	 * @param value
	 * @return
	 */
	protected Object toNetValue(Object value) {
		if(value == null)
			return null;
		// NetRange
		if(value instanceof Integer)
			return value;
		// value est une enum, NetEnum
		return value.toString();
	}
	
	/**
	 * Conversion d'une valeur net en valeur locale
	 * @param value
	 * @param targetClass
	 * @return
	 */
	protected Object fromNetValue(Object value, Class<?> targetClass) {
		if(value == null)
			return null;
		// NetRange
		if(targetClass.equals(Integer.class))
			return (Integer)value; // Doit déjà être un Integer
		// NetEnum
		if(!targetClass.isEnum())
			throw new UnsupportedOperationException();
		return Helpers.valueOfEnum(targetClass, (String)value);
	}
}
