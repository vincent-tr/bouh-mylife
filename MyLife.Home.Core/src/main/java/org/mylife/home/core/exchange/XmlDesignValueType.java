package org.mylife.home.core.exchange;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "valueType")
@XmlEnum(String.class)
public enum XmlDesignValueType {

	/**
	 * Les enums sont considérés comme des chaines avec une liste des valeurs
	 * possibles
	 */
	@XmlEnumValue("string")
	STRING,

	@XmlEnumValue("boolean")
	BOOLEAN,

	@XmlEnumValue("byte")
	BYTE,

	@XmlEnumValue("short")
	SHORT,

	@XmlEnumValue("int")
	INT,

	@XmlEnumValue("long")
	LONG,

	@XmlEnumValue("float")
	FLOAT,

	@XmlEnumValue("double")
	DOUBLE
}
