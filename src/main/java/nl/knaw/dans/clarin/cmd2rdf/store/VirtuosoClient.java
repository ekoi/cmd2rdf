/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.store;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import nl.knaw.dans.clarin.cmd2rdf.exception.ConverterException;
import nl.knaw.dans.clarin.cmd2rdf.mt.IAction;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Eko Indarto
 *
 */
public class VirtuosoClient extends RdfStore implements RdfHandler, IAction{
	private static final String NAMED_GRAPH_IRI = "graph-uri";
	private static final Logger log = LoggerFactory.getLogger(VirtuosoClient.class);
	private Client client;
	private String replacedPrefixBaseURI;
	private String prefixBaseURI;
	protected static String serverURL;
	protected static String username;
	protected static String password;
	public VirtuosoClient(){
		//super(getVirtuosoUrl(), getVirtuosoUser(), getVirtuosoPass());
		super(serverURL, username, password);
	}
	public VirtuosoClient(String serverURL, String username, String password) {
		super(serverURL,username, password);
		startUp();
	}

	public void startUp() {
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

	public boolean save(byte[] bytes, String graphUri) {
		try {
			UriBuilder uriBuilder = UriBuilder.fromUri(new URI(serverURL));
			graphUri = graphUri.replaceAll(" ", "_");
			uriBuilder.queryParam(NAMED_GRAPH_IRI, graphUri);
			WebTarget target = client.target(uriBuilder.build());
			Response response = target.request().post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));
			int status = response.getStatus();
			log.debug("response status: " + status);
			if ((status == Response.Status.CREATED.getStatusCode()) || (status == Response.Status.OK.getStatusCode()))
				return true;
		} catch (URISyntaxException e) {
			log.error("ERROR: URISyntaxException, caused by: " + e.getMessage());
		}
		
		return false;
	}

	public boolean delete(String filename) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getReplacedPrefixBaseURI() {
		return replacedPrefixBaseURI;
	}

	public void setReplacedPrefixBaseURI(String replacedPrefixBaseURI) {
		this.replacedPrefixBaseURI = replacedPrefixBaseURI;
	}

	public String getPrefixBaseURI() {
		return prefixBaseURI;
	}

	public void setPrefixBaseURI(String prefixBaseURI) {
		this.prefixBaseURI = prefixBaseURI;
	}
	public ByteArrayOutputStream execute(Object object) {
		// TODO Auto-generated method stub
		return null;
	}
	public void shutDown() throws ConverterException {
		// TODO Auto-generated method stub
		
	}
	


}
