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
import nl.knaw.dans.clarin.cmd2rdf.util.ActionStatus;
import nl.knaw.dans.clarin.cmd2rdf.util.Misc;

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
	private String serverURL;
	private String username;
	private String password;
	private ActionStatus act;
	

	public VirtuosoClient(){
	}

	public void startUp(Map<String, String> vars)
			throws ActionException {
		replacedPrefixBaseURI = vars.get("replacedPrefixBaseURI");
		prefixBaseURI = vars.get("prefixBaseURI");
		serverURL = vars.get("serverURL");
		username = vars.get("username");
		password = vars.get("password");
		String action = vars.get("action");
		
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
		if (action == null || action.isEmpty())
			throw new ActionException("action is null or empty");
		
		act = Misc.convertToActionStatus(action);
	
		log.debug("VirtuosoClient variables: ");
		log.debug("replacedPrefixBaseURI: " + replacedPrefixBaseURI);
		log.debug("prefixBaseURI: " + prefixBaseURI);
		log.debug("serverURL: " + serverURL);
		log.debug("username: " + username);
		log.debug("password: " + password);
		log.debug("action: " + action);
		log.debug("Start VirtuosoClient....");
		
//		ClientConfig clientConfig = new ClientConfig();
//		clientConfig.connectorProvider(new ApacheConnectorProvider());
//		client = ClientBuilder.newClient(clientConfig);
		client = ClientBuilder.newClient();
		HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.digest(username, password);
		client.register(authFeature);
	}

	public Object execute(String path, Object object) throws ActionException {
		boolean status = false;
		switch(act){
			case POST: status = uploadRdfToVirtuoso(path, object);
				break; 
			case DELETE: status = deleteRdfFromVirtuoso(path);
				break;
			default:
		}

		return status;
	}

private boolean deleteRdfFromVirtuoso(String path) {
	String gIRI = path.replace(".xml", ".rdf").replace(this.replacedPrefixBaseURI, this.prefixBaseURI).replaceAll(" ", "_");
	UriBuilder uriBuilder;
	try {
		uriBuilder = UriBuilder.fromUri(new URI(serverURL));
		uriBuilder.queryParam(NAMED_GRAPH_IRI, gIRI);
		WebTarget target = client.target(uriBuilder.build());
		Response response = target.request().delete();
		int status = response.getStatus();
		log.debug("Upload " + (path.replace(".xml", ".rdf")) + " to virtuoso server.\nResponse status: " + status);
		if ((status == Response.Status.CREATED.getStatusCode()) || (status == Response.Status.OK.getStatusCode()))
			return true;	
	} catch (URISyntaxException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	return false;
}

private boolean uploadRdfToVirtuoso(String path, Object object)
		throws ActionException {
	if (object instanceof Node) {
		log.debug("Upload '" + path.replace(".xml", ".rdf") + "'.");
		Node node = (Node)object;
		DOMSource source = new DOMSource(node);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(bos);
		try {
			TransformerFactory.newInstance().newTransformer().transform(source,result);
			String gIRI = path.replace(".xml", ".rdf").replace(this.replacedPrefixBaseURI, this.prefixBaseURI).replaceAll(" ", "_");
			UriBuilder uriBuilder = UriBuilder.fromUri(new URI(serverURL));
			uriBuilder.queryParam(NAMED_GRAPH_IRI, gIRI);
			WebTarget target = client.target(uriBuilder.build());
			byte[] bytes = bos.toByteArray();
			Response response = target.request().post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));
			int status = response.getStatus();
			log.debug("'" + (path.replace(".xml", ".rdf")) + "' is uploaded to virtuoso server.\nResponse status: " + status);
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
