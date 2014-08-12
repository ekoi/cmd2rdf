/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.batch;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Eko Indarto
 *
 */
@SuppressWarnings("restriction")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="cleanup")
public class Cleanup {
	@XmlElementWrapper(name = "actions")
	@XmlElement(name="action")
	List<Action> actions;
}