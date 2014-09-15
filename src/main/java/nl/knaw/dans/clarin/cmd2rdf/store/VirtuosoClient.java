package nl.knaw.dans.clarin.cmd2rdf.store;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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
import nl.knaw.dans.clarin.cmd2rdf.util.BytesConverter;
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
	private List<String> replacedPrefixBaseURI = new ArrayList<String>();
	private String prefixBaseURI;
	private String serverURL;
	private String username;
	private String password;
	private ActionStatus act;
	private static int n;
	

	public VirtuosoClient(){
	}

	public void startUp(Map<String, String> vars)
			throws ActionException {
		String replacedPrefixBaseURIVar = vars.get("replacedPrefixBaseURI");
		prefixBaseURI = vars.get("prefixBaseURI");
		serverURL = vars.get("serverURL");
		username = vars.get("username");
		password = vars.get("password");
		String action = vars.get("action");
		
		if (replacedPrefixBaseURIVar == null || replacedPrefixBaseURIVar.isEmpty())
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
		
		String replacedPrefixBaseURIVars[] = replacedPrefixBaseURIVar.split(",");
		for (String s:replacedPrefixBaseURIVars) {
			if (!s.trim().isEmpty())
				replacedPrefixBaseURI.add(s);
		}
		
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
//		client = ClientBuilder.newClient();
//		HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.digest(username, password);
//		client.register(authFeature);
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
	
	UriBuilder uriBuilder;
	try {
		String gIRI = getGIRI(path);
		uriBuilder = UriBuilder.fromUri(new URI(serverURL));
		uriBuilder.queryParam(NAMED_GRAPH_IRI, gIRI);
		WebTarget target = client.target(uriBuilder.build());
		Response response = target.request().delete();
		int status = response.getStatus();
		log.debug("Upload " + (path.replace(".xml", ".rdf")) + " to virtuoso server.\nResponse status: " + status);
		if ((status == Response.Status.CREATED.getStatusCode()) || (status == Response.Status.OK.getStatusCode())){
			n++;
			log.debug("[" + n + "] is DELETED.");
			return true;	
		} else {
			log.error(">>>>>>>>>> ERROR: " + status);
		}
	} catch (URISyntaxException e) {
		log.error("ERROR: URISyntaxException, caused by " + e.getMessage());
		e.printStackTrace();
	} catch (ActionException e) {
		log.error("ERROR: ActionException, caused by " + e.getMessage());
	}
	
	return false;
}

private String getGIRI(String path) throws ActionException {
	String gIRI = null;
	for (String s:replacedPrefixBaseURI) {
		if (path.startsWith(s)) {
			gIRI = path.replace(s, this.prefixBaseURI).replace(".xml", ".rdf").replaceAll(" ", "_");
			break;
		}
	}
	if (gIRI==null)
		throw new ActionException("gIRI ERROR: " + path + " is not found as prefix in " + replacedPrefixBaseURI);
	return gIRI;
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
			log.debug("START transformation from DOMSource to RD.F");
			long startTrans = System.currentTimeMillis();
			TransformerFactory.newInstance().newTransformer().transform(source,result);
			log.debug("END transformation from DOMSource to RDF. Duration: " + BytesConverter.friendly(System.currentTimeMillis() - startTrans) + " milliseconds.");
			
			byte[] bytes = bos.toByteArray();
			log.debug("BYTES SIZE: " + BytesConverter.friendly(bytes.length));
			
			long startUplod = System.currentTimeMillis();
			
			String gIRI = getGIRI(path);
			UriBuilder uriBuilder = UriBuilder.fromUri(new URI(serverURL));
			uriBuilder.queryParam(NAMED_GRAPH_IRI, gIRI);
			client = ClientBuilder.newClient();
			HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.digest(username, password);
			client.register(authFeature);
			
			WebTarget target = client.target(uriBuilder.build());
			
			Response response = target.request().post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));
			int status = response.getStatus();
			log.debug("'" + (path.replace(".xml", ".rdf")) + "' is uploaded to virtuoso server.\nResponse status: " + status);
			if ((status == Response.Status.CREATED.getStatusCode()) || (status == Response.Status.OK.getStatusCode())){
				n++;
				log.debug("[" + n + "] is CREATED. Duration: " + (System.currentTimeMillis() - startUplod) + " milliseconds.");
				return true;	
			} else {
				log.error(">>>>>>>>>> ERROR: " + status);
			}
			client.close();
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
	}
	
	@Override
	public String name() {
		
		return this.getClass().getName();
	}
}
