/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.batch;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
/**
 * @author akmi
 *
 */
@SuppressWarnings("restriction")
@XmlAccessorType(XmlAccessType.FIELD)
public class Arg {
	@XmlAttribute
	String name;
	
	@XmlValue
	String value;
}
