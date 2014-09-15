package nl.knaw.dans.clarin.cmd2rdf.store;

import java.io.File;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.knaw.dans.clarin.cmd2rdf.exception.ActionException;
import nl.knaw.dans.clarin.cmd2rdf.mt.IAction;
import nl.knaw.dans.clarin.cmd2rdf.util.BytesConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * @author Eko Indarto
 *
 */
public class FileStore implements IAction{
	private static final Logger log = LoggerFactory.getLogger(FileStore.class);
	private String xmlSourceDir;
	private String rdfOutputDir;
	

	public FileStore(){
	}

	public void startUp(Map<String, String> vars)
			throws ActionException {
		xmlSourceDir = vars.get("xmlSourceDir");
		rdfOutputDir = vars.get("rdfOutpuDir");
		
		if (xmlSourceDir == null || xmlSourceDir.isEmpty())
			throw new ActionException("xmlSourceDir is null or empty");
		
		if (rdfOutputDir == null || rdfOutputDir.isEmpty())
			throw new ActionException("rdfOutputDir is null or empty");
		
	
		log.debug("Save the RDF files to " + rdfOutputDir);
		
	}

	public Object execute(String path, Object object) throws ActionException {
		boolean status = saveRdfToFileSystem(path, object);

		return status;
	}


private boolean saveRdfToFileSystem(String path, Object object)
		throws ActionException {
	if (object instanceof Node) {
		log.debug("Save '" + path.replace(".xml", ".rdf") + "'.");
		Node node = (Node)object;
		DOMSource source = new DOMSource(node);
		try {
			long l = System.currentTimeMillis();
			String rdfFileOutputName = path.replace(xmlSourceDir,  rdfOutputDir).replace(".xml", ".rdf");
			TransformerFactory.newInstance().newTransformer().transform(source,new StreamResult(new File(rdfFileOutputName)));
			log.debug("Save duration: " + (BytesConverter.friendly(System.currentTimeMillis() - l )));
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by " + e.getMessage());
		} catch (TransformerException e) {
			log.error("ERROR: TransformerException, caused by " + e.getMessage());
		} catch (TransformerFactoryConfigurationError e) {
			log.error("ERROR: TransformerFactoryConfigurationError, caused by " + e.getMessage());
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
