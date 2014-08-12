/**
 * 
 */
package nl.knaw.dans.clarin.playground;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.knaw.dans.clarin.cmd2rdf.exception.ImportException;
import nl.knaw.dans.clarin.cmd2rdf.store.VirtuosoClient;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

/**
 * @author Eko Indarto
 *
 */
public class VirtuosoImporter{

	public boolean uploadRdfFile(InputStream rdfStream) throws ImportException {
		 // Creating JAX-RS client
		Client client = ClientBuilder.newClient();
		//authenticate
		HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.digest("dba", "dba");
		client.register(authFeature);

		WebTarget target = client.target("http://localhost:8000/sparql-graph-crud-auth?graph-uri=http://localhost:8890/DAV/X777/oai_SinicaCorpus_sinica_edu_tw_SinicaCorpus.rdf");

//	
        InputStream is = null;
		try {
			byte[] bytes = FileUtils.readFileToByteArray(new File("/Users/akmi/eko77/oai_SinicaCorpus_sinica_edu_tw_SinicaCorpus.rdf"));
			is = new ByteArrayInputStream(bytes);
			Response response = target.request()
                    .put(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));
		} catch (IOException e) {
			e.printStackTrace();
		}  
		if (is!=null) {
			
			
		//System.out.println(response.readEntity(String.class));
		} else {
			System.out.println("llll");
		}
		
		return false;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		VirtuosoImporter vi = new VirtuosoImporter();
//		try {
//			vi.uploadRdfFile(null);
//		} catch (ImportException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		VirtuosoClient vs = new VirtuosoClient("http://localhost:8000/sparql-graph-crud-auth", "dba", "dba");
		try {
			int i=0;
			//Collection<File> listFiles = FileUtils.listFiles(new File("/Users/akmi/eko77"),new String[] {"rdf"}, true);
			//for (File file:listFiles) {
				//String filepath = file.getAbsolutePath().replace("/Users/akmi/eko77", "http://localhost:8890/DAV/Q" + i++);
			byte[] bytes = FileUtils.readFileToByteArray(new File("/Users/akmi/ajinomoto.rdf"));
			
			boolean b = vs.save(bytes, "http://localhost:8988/DAV/X19YX/oai_beeldengeluid_nl_Expressie_1000278.rdf");
			
//			byte[] bytes = FileUtils.readFileToByteArray(new File("/Users/akmi/Dropbox/DANS/IN_PROGRESS/CMDI2RDF-Workspace/data/cmd-rdf/oai_beeldengeluid_nl_Expressie_1000278.rdf"));
//			
//			boolean b = vs.save(bytes, "http://localhost:8890/DAV/4YYYY/oai_beeldengeluid_nl_Expressie_1000278.rdf");
			System.out.println("\t" + b);
			//}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}  
	}
	
}
