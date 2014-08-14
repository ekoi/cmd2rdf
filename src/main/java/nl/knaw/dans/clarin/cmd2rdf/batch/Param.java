package nl.knaw.dans.clarin.cmd2rdf.batch;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@SuppressWarnings("restriction")
@XmlRootElement (name="param")
@XmlAccessorType(XmlAccessType.FIELD)
public class Param {
	@XmlAttribute
	String name;
	
	@XmlValue
	String value;
	
//	@XmlElement(name="argument")
//	private Stylesheet stylesheet;
//	
//	public Stylesheet getStylesheet() {
//		return stylesheet;
//	}
//
//	public void setStylesheet(Stylesheet stylesheet) {
//		this.stylesheet = stylesheet;
//	}

}
