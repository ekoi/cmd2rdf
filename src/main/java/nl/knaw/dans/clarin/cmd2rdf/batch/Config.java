package nl.knaw.dans.clarin.cmd2rdf.batch;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@SuppressWarnings("restriction")
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {
	
	@XmlAttribute
	String version;
	
	int numberOfThreads;
	String xslSourceDir;
	String harvestedRecordsLocation;
	String rdfOutpuDirectory;
	String profileCacheLocation;
	String baseUri;
	String virtuosoUrl;
}
