/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.store;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.knaw.dans.clarin.cmd2rdf.exception.ActionException;
import nl.knaw.dans.clarin.cmd2rdf.mt.IAction;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * @author Eko Indarto
 *
 */
public class VirtuosoClient implements IAction{
	private static final String NAMED_GRAPH_IRI = "graph-uri";
	private static final Logger log = LoggerFactory.getLogger(VirtuosoClient.class);
	private Client client;
	private String replacedPrefixBaseURI;
	private String prefixBaseURI;
	protected String serverURL;
	protected String username;
	protected String password;

	public VirtuosoClient(){
	}

	public void startUp(Map<String, String> vars)
			throws ActionException {
		this.replacedPrefixBaseURI = vars.get("replacedPrefixBaseURI");
		this.prefixBaseURI = vars.get("prefixBaseURI");
		this.serverURL = vars.get("serverURL");
		this.username = vars.get("username");
		this.password = vars.get("password");

		if (replacedPrefixBaseURI == null || replacedPrefixBaseURI.isEmpty())
			throw new ActionException("replacedPrefixBaseURI is null or empty");
		if (prefixBaseURI == null || prefixBaseURI.isEmpty())
			throw new ActionException("prefixBaseURI is null or empty");
		if (serverURL == null || serverURL.isEmpty())
			throw new ActionException("serverURL is null or empty");
		if (username == null || username.isEmpty())
			throw new ActionException("username is null or empty");
		if (password == null || password.isEmpty())
			throw new ActionException("password is null or empty");
	
		log.debug("VirtuosoClient variables: ");
		log.debug("replacedPrefixBaseURI: " + replacedPrefixBaseURI);
		log.debug("prefixBaseURI: " + prefixBaseURI);
		log.debug("serverURL: " + serverURL);
		log.debug("username: " + username);
		log.debug("password: " + password);
		log.debug("Start VirtuosoClient....");
		
//		ClientConfig clientConfig = new ClientConfig();
//		clientConfig.connectorProvider(new ApacheConnectorProvider());
//		client = ClientBuilder.newClient(clientConfig);
		client = ClientBuilder.newClient();
		HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.digest(username, password);
		client.register(authFeature);
	}
//curl --digest --user dba:dba --verbose --url "http://example.com/sparql-graph-crud-auth?graph-uri=urn:graph:update:test:put" -T books.ttl 
//curl --digest --user dba:dba --verbose --url "http://localhost:8000/sparql-graph-crud-auth?graph-uri=http://localhost:8880/DAV/
	//xx/oai_SinicaCorpus_sinica_edu_tw_SinicaCorpus.rdf" -T /Users/akmi/eko77/oai_SinicaCorpus_sinica_edu_tw_SinicaCorpus.rdf	
	public Object execute(String path, Object object) throws ActionException {
		byte[] bytes = null;
		// prepare input
		if (object instanceof Node) {
			Node node = (Node)object;
			DOMSource source = new DOMSource(node);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(bos);
			try {
				TransformerFactory.newInstance().newTransformer().transform(source,result);
				bytes = bos.toByteArray();
				String gIRI = path.replace(".xml", ".rdf").replace(this.replacedPrefixBaseURI, this.prefixBaseURI).replaceAll(" ", "_");
				UriBuilder uriBuilder = UriBuilder.fromUri(new URI(serverURL));
				uriBuilder.queryParam(NAMED_GRAPH_IRI, gIRI);
				WebTarget target = client.target(uriBuilder.build());
				Response response = target.request().post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));
				//Response response = target.request().delete();
				int status = response.getStatus();
				log.debug("Upload " + (path.replace(".xml", ".rdf")) + " to virtuoso server.\nResponse status: " + status);
				if ((status == Response.Status.CREATED.getStatusCode()) || (status == Response.Status.OK.getStatusCode()))
					return true;
			} catch (TransformerConfigurationException e) {
				log.error("ERROR: TransformerConfigurationException, caused by " + e.getMessage());
			} catch (TransformerException e) {
				log.error("ERROR: TransformerException, caused by " + e.getMessage());
			} catch (TransformerFactoryConfigurationError e) {
				log.error("ERROR: TransformerFactoryConfigurationError, caused by " + e.getMessage());
			} catch (URISyntaxException e) {
				log.error("ERROR: URISyntaxException, caused by " + e.getMessage());
			}
			
		} else
			throw new ActionException("Unknown input ("+path+", "+object+")");

		return false;
	}
	

	public void shutDown() throws ActionException {
		// TODO Auto-generated method stub
		
	}
	


}
