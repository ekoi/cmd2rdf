package nl.knaw.dans.clarin.cmd2rdf.batch;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@SuppressWarnings("restriction")
@XmlAccessorType(XmlAccessType.FIELD)
public class Record {
	
	@XmlAttribute
	String filter;
	
	@XmlAttribute
	String className;
	
	@XmlAttribute
	String methodToExecute;
	
	@XmlElementWrapper(name = "properties")
	@XmlElement(name="property")
	List<Property> property;
	
	@XmlElementWrapper(name = "actions")
	@XmlElement(name="action")
	List<Action> actions;
}

