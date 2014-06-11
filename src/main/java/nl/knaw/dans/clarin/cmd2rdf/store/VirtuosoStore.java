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

/**
 * @author Eko Indarto
 *
 */
public class VirtuosoStore extends RdfStore implements RdfHandler{
	private static final String NAMED_GRAPH_IRI = "graph-uri";
	private Client client;
	public VirtuosoStore(String serverURL, int port, String path, String username, String password) {
		super(serverURL, port, path,username, password);
		init();
	}

	private void init() {
		 // Creating JAX-RS client
		client = ClientBuilder.newClient();
		//authenticate
		HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.digest(getUsername(), getPassword());
		client.register(authFeature);
	}

	public boolean save(byte[] bytes, String graphUri) {
		try {
			UriBuilder uriBuilder = UriBuilder.fromUri(new URI(getServerURL()));
			uriBuilder.port(getPort());
			uriBuilder.path(getPath());
			uriBuilder.queryParam(NAMED_GRAPH_IRI, graphUri);
			
			WebTarget target = client.target(uriBuilder.build());
			Response response = target.request()
                    .put(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));
			if (response.getStatus() == 201)
				return true;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	public boolean delete(String filename) {
		// TODO Auto-generated method stub
		return false;
	}


}
