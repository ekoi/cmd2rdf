/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.store;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Eko Indarto
 *
 */
public class VirtuosoStore extends RdfStore implements RdfHandler{
	private static final String NAMED_GRAPH_IRI = "graph-uri";
	private static final Logger log = LoggerFactory.getLogger(VirtuosoStore.class);
	private Client client;
	public VirtuosoStore(String serverURL, String username, String password) {
		super(serverURL,username, password);
		init();
	}

	private void init() {
//		ClientConfig clientConfig = new ClientConfig();
//		clientConfig.connectorProvider(new ApacheConnectorProvider());
//		client = ClientBuilder.newClient(clientConfig);
		client = ClientBuilder.newClient();
		HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.digest(getUsername(), getPassword());
		client.register(authFeature);
	}

	public boolean save(byte[] bytes, String graphUri) {
		try {
			UriBuilder uriBuilder = UriBuilder.fromUri(new URI(getServerURL()));
			uriBuilder.queryParam(NAMED_GRAPH_IRI, graphUri);
			
			WebTarget target = client.target(uriBuilder.build());
			Response response = target.request()
                    .post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));
			if (response.getStatus() == 201)
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


}
