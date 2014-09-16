package nl.knaw.dans.clarin.cmd2rdf.store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.knaw.dans.clarin.cmd2rdf.exception.ActionException;
import nl.knaw.dans.clarin.cmd2rdf.mt.IAction;

import org.apache.commons.io.FileUtils;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.joda.time.Period;
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
	private List<String> replacedPrefixBaseURI = new ArrayList<String>();
	private String prefixBaseURI;

	public FileStore(){
	}

	public void startUp(Map<String, String> vars)
			throws ActionException {
		xmlSourceDir = vars.get("xmlSourceDir");
		rdfOutputDir = vars.get("rdfOutputDir");
		String replacedPrefixBaseURIVar = vars.get("replacedPrefixBaseURI");
		prefixBaseURI = vars.get("prefixBaseURI");
		if (replacedPrefixBaseURIVar == null || replacedPrefixBaseURIVar.isEmpty())
			throw new ActionException("replacedPrefixBaseURI is null or empty");
		if (prefixBaseURI == null || prefixBaseURI.isEmpty())
			throw new ActionException("prefixBaseURI is null or empty");
		if (xmlSourceDir == null || xmlSourceDir.isEmpty())
			throw new ActionException("xmlSourceDir is null or empty");
		
		if (rdfOutputDir == null || rdfOutputDir.isEmpty())
			throw new ActionException("rdfOutputDir is null or empty");
		String replacedPrefixBaseURIVars[] = replacedPrefixBaseURIVar.split(",");
		for (String s:replacedPrefixBaseURIVars) {
			if (!s.trim().isEmpty())
				replacedPrefixBaseURI.add(s);
		}
	
		log.debug("Save the RDF files to " + rdfOutputDir);
		
	}

	public Object execute(String path, Object object) throws ActionException {
		Split split = SimonManager.getStopwatch("stopwatch.filestore").start();
		boolean status = saveRdfToFileSystem(path, object);
		split.stop();
		return status;
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
			FileUtils.write(new File(rdfFileOutputName+".graph"), getGIRI(path));
			long duration = (System.currentTimeMillis() - l );
			log.debug("Save duration: " + duration + " ms.");
			if (duration > 5000) {
				Period p = new Period(duration);
				log.debug("Saving took more than 4 seconds. It took " + p.getSeconds() + " secs.");
			}
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by " + e.getMessage());
		} catch (TransformerException e) {
			log.error("ERROR: TransformerException, caused by " + e.getMessage());
		} catch (TransformerFactoryConfigurationError e) {
			log.error("ERROR: TransformerFactoryConfigurationError, caused by " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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