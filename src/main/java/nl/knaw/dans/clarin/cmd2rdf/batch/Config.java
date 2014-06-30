package nl.knaw.dans.clarin.cmd2rdf.batch;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@SuppressWarnings("restriction")
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {
	
	@XmlAttribute
	String version;
	
	String homedir;
	String xslDir;
	String xmlDir;
	String harvestedRecordDir;
	String rdfOutputDir;
	String profileCacheLoc;
	String baseUri;
	String virtuosoUrl;
	String virtuosoUser;
	String virtuosoPass;
	public String getVersion() {
		return version;
	}
	public String getHomedir() {
		return homedir;
	}
	public String getXslDir() {
		return xslDir;
	}
	public String getXmlDir() {
		return xmlDir;
	}
	public String getHarvestedRecordDir() {
		return harvestedRecordDir;
	}
	public String getRdfOutputDir() {
		return rdfOutputDir;
	}
	public String getProfileCacheLoc() {
		return profileCacheLoc;
	}
	public String getBaseUri() {
		return baseUri;
	}
	public String getVirtuosoUrl() {
		return virtuosoUrl;
	}
	public String getVirtuosoUser() {
		return virtuosoUser;
	}
	public String getVirtuosoPass() {
		return virtuosoPass;
	}
}
