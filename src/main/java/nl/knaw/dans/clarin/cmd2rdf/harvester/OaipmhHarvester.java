/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.harvester;

import java.util.List;

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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String baseUrl = "http://easy.dans.knaw.nl/oai";
		OaiPmhServer server = new OaiPmhServer(baseUrl);

		String prefix = "oai_dc";
		String set = null;
		try {
			RecordsList records = server.listRecords(
					prefix, null, null,
					set);
			boolean more = true;
			while (more) {
				for (Record record : records.asList()) {
					
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
	}

}
