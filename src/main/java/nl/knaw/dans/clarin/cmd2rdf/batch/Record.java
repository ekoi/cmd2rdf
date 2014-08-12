package nl.knaw.dans.clarin.cmd2rdf.batch;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@SuppressWarnings("restriction")
@XmlAccessorType(XmlAccessType.FIELD)
public class Record implements Runnable{
	
	@XmlAttribute
	String filter;
	
	@XmlAttribute
	int nThreads;
	
	@XmlAttribute
	String xmlSource;
	
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

	public void run() {
		if (actions == null)
		System.out.println("_______________ NULL NULL _______________");
		else {
			System.out.println("_______________ HELLO RUN _______________");
		}
	}
}

