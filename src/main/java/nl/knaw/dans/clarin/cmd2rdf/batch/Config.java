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
	String harvestedProfilesDir;
	String xsltSourceDir;
	String xmlSourceDir;
	String urlDB;
	String rdfStoreServerURL;
	String rdfStoreUsername;
	String rdfStorePass;
	public String getVersion() {
		return version;
	}
	public String getHomedir() {
		return homedir;
	}
	public String getHarvestedProfilesDir() {
		return harvestedProfilesDir;
	}
	public String getXsltSourceDir() {
		return xsltSourceDir;
	}
	public String getXmlSourceDir() {
		return xmlSourceDir;
	}
	public String getUrlDB() {
		return urlDB;
	}
	public String getRdfStoreServerURL() {
		return rdfStoreServerURL;
	}
	public String getRdfStoreUsername() {
		return rdfStoreUsername;
	}
	public String getRdfStorePass() {
		return rdfStorePass;
	}
}
