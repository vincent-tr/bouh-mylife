package org.mylife.home.net.exchange;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "type")
@XmlSeeAlso({ XmlNetEnum.class, XmlNetRange.class })
public class XmlNetType {

}
