/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.harvester;

import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;

/**
 * @author Eko Indarto
 *
 */
public class OaipmhHarvester {
	private static final Logger log = LoggerFactory.getLogger(OaipmhHarvester.class);
	private static boolean asRoot = true;
	private static final String PREFIFX = "oai_rdf";
	private static final String SET = "meertens:VLO-orgs";
	private String baseUrl;
	private String outputFile;
	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public void harvest(){
		log.debug("Harvesting process.");
		Document doc = DocumentFactory.getInstance()
				.createDocument();
		Element rootElement = null;
		OaiPmhServer server = new OaiPmhServer(baseUrl);
		log.debug("baseUrl: " + baseUrl);
		log.debug("prefix: " + PREFIFX);
		log.debug("SET: " + SET);
		try {
			RecordsList records = server.listRecords(
					PREFIFX, null, null,
					SET);
			boolean more = true;
			while (more) {
				for (Record record : records.asList()) {
					if (record != null) {
						Element element = record.getMetadata();
						
						if (element != null) {
							Node node = element.selectSingleNode("rdf:Description");
							if (asRoot){
								rootElement = node.getParent();
								boolean b = rootElement.remove(node);
								rootElement = rootElement.createCopy();
								asRoot = false;
							} 
							rootElement.add(node.detach());
						}
					}
				}
				if (records.getResumptionToken() != null) {
					ResumptionToken rt = records.getResumptionToken();
						//Thread.sleep(3000);
					records = server.listRecords(rt);
				} else {
					more = false;
				}
			}
			
		} catch (OAIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doc.add(rootElement);
		 // lets write to a file
        XMLWriter writer;
		try {
			writer = new XMLWriter(
			    new FileWriter( outputFile )
			);
			log.debug("Writing rdf file to " + outputFile);
			writer.write( doc );
			 writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String getBaseUrl() {
		return baseUrl;
	}
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	public String getOutputFile() {
		return outputFile;
	}
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

}
