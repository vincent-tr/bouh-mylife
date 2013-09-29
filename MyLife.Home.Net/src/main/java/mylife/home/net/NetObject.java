package mylife.home.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mylife.home.net.structure.NetAction;
import mylife.home.net.structure.NetAttribute;
import mylife.home.net.structure.NetClass;
import mylife.home.net.structure.NetEnum;
import mylife.home.net.structure.NetMember;
import mylife.home.net.structure.NetRange;
import mylife.home.net.structure.NetType;

/**
 * Objet instanci�
 * 
 * @author pumbawoman
 * 
 */
public class NetObject {

	private static void checkValue(NetType type, Object value)
			throws InvalidValueException {
		if (type instanceof NetRange) {
			NetRange nrange = (NetRange) type;
			if (!(value instanceof Integer))
				throw new InvalidValueException();
			int ival = ((Integer) value).intValue();
			if (ival < nrange.getMin())
				throw new InvalidValueException();
			if (ival > nrange.getMax())
				throw new InvalidValueException();
		} else if (type instanceof NetEnum) {
			NetEnum nenum = (NetEnum) type;
			if (!(value instanceof String))
				throw new InvalidValueException();
			String sval = (String) value;
			boolean found = false;
			for (String eval : nenum.getValues()) {
				if (eval.equals(sval)) {
					found = true;
					break;
				}
			}
			if (!found)
				throw new InvalidValueException();
		} else
			throw new InvalidValueException(); // bad type
	}

	private static void checkActionArguments(NetAction action,
			Object[] arguments) throws InvalidValueException {
		List<NetType> argTypes = action.getArguments();
		if (argTypes.size() != arguments.length)
			throw new InvalidValueException();
		for (int i = 0; i < arguments.length; i++)
			checkValue(argTypes.get(i), arguments[i]);
	}

	private static Object convertValue(NetType type, String value)
			throws InvalidValueException {
		if (type instanceof NetRange) {
			NetRange nrange = (NetRange) type;
			Integer cvalue;
			try {
				cvalue = new Integer(value);
			} catch (NumberFormatException ex) {
				throw new InvalidValueException(ex);
			}
			int ival = ((Integer) cvalue).intValue();
			if (ival < nrange.getMin())
				throw new InvalidValueException();
			if (ival > nrange.getMax())
				throw new InvalidValueException();
			return cvalue;
		} else if (type instanceof NetEnum) {
			NetEnum nenum = (NetEnum) type;
			boolean found = false;
			for (String eval : nenum.getValues()) {
				if (eval.equals(value)) {
					found = true;
					break;
				}
			}
			if (!found)
				throw new InvalidValueException();
			return value;
		} else
			throw new InvalidValueException(); // bad type
	}

	private static Object[] convertActionArguments(NetAction action,
			String[] arguments) throws InvalidValueException {
		List<NetType> argTypes = action.getArguments();
		List<Object> ret = new ArrayList<Object>();
		if (argTypes.size() != arguments.length)
			throw new InvalidValueException();
		for (int i = 0; i < arguments.length; i++)
			ret.add(convertValue(argTypes.get(i), arguments[i]));
		return ret.toArray();
	}

	private static class ObjectAttribute {
		private final NetObject owner;
		private final NetAttribute attribute;
		private Object value;
		private final Collection<AttributeChangeListener> listeners = new ArrayList<AttributeChangeListener>();

		public ObjectAttribute(NetObject owner, NetAttribute attribute) {
			this.owner = owner;
			this.attribute = attribute;
		}

		public Object getAttributeValue() {
			return value;
		}

		public void setAttributeValue(Object value)
				throws InvalidValueException {
			checkValue(attribute.getType(), value);
			this.value = value;
			for (AttributeChangeListener listener : listeners)
				listener.attributeChanged(owner, attribute, value);
		}

		public void registerAttributeChange(AttributeChangeListener listener) {
			listeners.add(listener);
		}

		public void unregisterAttributeChange(AttributeChangeListener listener) {
			listeners.remove(listener);
		}
	}

	private class ObjectAction {
		private final NetObject owner;
		private final NetAction action;
		private ActionExecutor executor;

		public ObjectAction(NetObject owner, NetAction action) {
			this.owner = owner;
			this.action = action;
		}

		public void setActionExecutor(ActionExecutor executor) {
			this.executor = executor;
		}

		public void executeAction(Object[] arguments)
				throws ActionNotImplementedException, InvalidValueException {
			checkActionArguments(action, arguments);
			executeActionUnchecked(arguments);
		}

		public void executeActionAsString(String[] arguments)
				throws ActionNotImplementedException, InvalidValueException {
			Object[] oarguments = convertActionArguments(action, arguments);
			executeActionUnchecked(oarguments);
		}

		private void executeActionUnchecked(Object[] arguments) {
			if (executor == null)
				throw new ActionNotImplementedException();
			executor.execute(owner, action, arguments);
		}
	}

	private final String id;
	private final NetClass netClass;
	private final Map<String, ObjectAttribute> attributes;
	private final Map<String, ObjectAction> actions;

	public NetObject(String id, NetClass netClass) {
		this.id = id;
		this.netClass = netClass;

		this.attributes = new HashMap<String, ObjectAttribute>();
		this.actions = new HashMap<String, ObjectAction>();
		for (NetMember member : netClass.getMembers()) {
			String name = member.getName().toLowerCase(Locale.ROOT);
			if (member instanceof NetAttribute) {
				NetAttribute attribute = (NetAttribute) member;
				attributes.put(name, new ObjectAttribute(this,
						attribute));
			} else if (member instanceof NetAction) {
				NetAction action = (NetAction) member;
				actions.put(name, new ObjectAction(this, action));
			}
		}
	}

	public String getId() {
		return id;
	}

	public NetClass getNetClass() {
		return netClass;
	}

	private ObjectAction getAction(String name) throws MemberNotFoundException {
		name = name.toLowerCase(Locale.ROOT);
		if (!actions.containsKey(name))
			throw new MemberNotFoundException();
		return actions.get(name);
	}

	private ObjectAttribute getAttribute(String name) throws MemberNotFoundException {
		name = name.toLowerCase(Locale.ROOT);
		if (!attributes.containsKey(name))
			throw new MemberNotFoundException();
		return attributes.get(name);
	}

	public void setActionExecutor(String name, ActionExecutor executor)
			throws MemberNotFoundException {
		ObjectAction action = getAction(name);
		action.setActionExecutor(executor);
	}

	/**
	 * Exécution d'une action
	 * 
	 * @param name
	 * @param arguments
	 * @throws ActionNotImplementedException
	 * @throws MemberNotFoundException
	 * @throws InvalidValueException
	 */
	public void executeAction(String name, Object[] arguments)
			throws ActionNotImplementedException, MemberNotFoundException,
			InvalidValueException {
		ObjectAction action = getAction(name);
		action.executeAction(arguments);
	}

	/**
	 * Exécution d'une action avec les paramètres fournis comme des chaines
	 * 
	 * @param name
	 * @param arguments
	 * @throws ActionNotImplementedException
	 * @throws MemberNotFoundException
	 * @throws InvalidValueException
	 */
	public void executeActionAsString(String name, String[] arguments)
			throws ActionNotImplementedException, MemberNotFoundException,
			InvalidValueException {
		ObjectAction action = getAction(name);
		action.executeActionAsString(arguments);
	}
	
	public Object getAttributeValue(String name) throws MemberNotFoundException {
		return getAttribute(name).getAttributeValue();
	}

	public void setAttributeValue(String name, Object value)
			throws MemberNotFoundException, InvalidValueException {
		getAttribute(name).setAttributeValue(value);
	}

	public void registerAttributeChange(String name,
			AttributeChangeListener listener) throws MemberNotFoundException {
		getAttribute(name).registerAttributeChange(listener);
	}

	public void unregisterAttributeChange(String name,
			AttributeChangeListener listener) throws MemberNotFoundException {
		getAttribute(name).unregisterAttributeChange(listener);
	}
}
