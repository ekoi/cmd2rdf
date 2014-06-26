package nl.knaw.dans.clarin.cmd2rdf.batch;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.XmlMixed;

@SuppressWarnings("restriction")
@XmlAccessorType(XmlAccessType.FIELD)
public class Argument {
	@XmlAttribute
	String name;
	
	@XmlElement(name="param")
	List<Param> params;
	
}
