/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.harvester;

import java.io.File;
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
	private String oaipmhBaseURL;
	private String prefix;
	private String set;
	private String outputFile;
	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public void harvest(){
		log.debug("OaipmhHarvester variables: ");
		log.debug("baseUrl: " + oaipmhBaseURL);
		log.debug("prefix: " + prefix);
		log.debug("set: " + set);
		log.debug("outputFile: " + outputFile);
		log.debug("Start Harvesting....");
		
		File file = new File(outputFile);
		if (file.exists()) {
			log.debug(outputFile + "is exists.");
			log.debug("Deleting file...");
			boolean ok = file.delete();
			if (ok)
				log.debug(outputFile + " is deleted.");
			else
				log.error("Cannot delete the " + outputFile + " file.");
		}
		
		Document doc = DocumentFactory.getInstance()
				.createDocument();
		Element rootElement = null;
		OaiPmhServer server = new OaiPmhServer(oaipmhBaseURL);
		log.debug("baseUrl: " + oaipmhBaseURL);
		log.debug("prefix: " + prefix);
		log.debug("SET: " + set);
		try {
			RecordsList records = server.listRecords(
					prefix, null, null,
					set);
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
								if (b) {
									rootElement = rootElement.createCopy();
									asRoot = false;
								} else {
									log.error("ERROR on harvest method.");
								}
							} 
							rootElement.add(node.detach());
						}
					}
				}
				if (records.getResumptionToken() != null) {
					log.debug("Harvest the next token.");
					ResumptionToken rt = records.getResumptionToken();
					Thread.sleep(1000);
					records = server.listRecords(rt);
				} else {
					more = false;
				}
			}
			log.debug("Harvesting is finish.");
		} catch (OAIException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
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
}
