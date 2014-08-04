package nl.knaw.dans.clarin.cmd2rdf.batch;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="CMD2RDF")
public class Jobs {
	private Config config;
	private Prepare prepare;
	private Record record;
	
	public void setConfig(Config config) {
		this.config = config;
	}
	public void setRecord(Record record) {
		this.record = record;
	}
	
	public Config getConfig() {
		return config;
	}
	public Record getRecord() {
		return record;
	}
	public Prepare getPrepare() {
		return prepare;
	}
	public void setPrepare(Prepare prepare) {
		this.prepare = prepare;
	}
}
