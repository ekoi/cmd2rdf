/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.harvester;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.XMLWriter;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;

/**
 * @author akmi
 *
 */
public class OaipmhHarvester {
	private static boolean asRoot = true;

	/**
	 * @param args
	 * @throws DocumentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws DocumentException, IOException {
		Document doc = DocumentFactory.getInstance()
				.createDocument();
		Element rootElement = null;
		String baseUrl = "https://openskos.meertens.knaw.nl/oai-pmh";
		OaiPmhServer server = new OaiPmhServer(baseUrl);

		String prefix = "oai_rdf";
		String set = "meertens:VLO-orgs";
		try {
			RecordsList records = server.listRecords(
					prefix, null, null,
					set);
			boolean more = true;
			int x = 0;
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
		System.out.println(doc.asXML());
		 // lets write to a file
        XMLWriter writer = new XMLWriter(
            new FileWriter( "/Users/akmi/output.rdf" )
        );
        writer.write( doc );
        writer.close();
	}

}
