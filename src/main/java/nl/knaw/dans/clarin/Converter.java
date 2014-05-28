package nl.knaw.dans.clarin;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Eko Indarto
 *
 */
public class Converter {
    /** 
     * Simple transformation method. 
     * @param sourcePath - Absolute path to source xml file. 
     * @param xsltPath - Absolute path to xslt file. 
     * @param resultDir - Directory where you want to put resulting files. 
     */  
	
	private static final Logger log = LoggerFactory.getLogger(Converter.class);
	private String xsltPath;
	private String cacheBasePathDir;
	private Templates cachedXSLT;
	private Transformer transformer;
	
	public Converter(String xsltPath, String cacheBasePathDir){
		this.xsltPath = xsltPath;
		this.cacheBasePathDir = cacheBasePathDir;
		log.debug("xsltPath: " + xsltPath);
		log.debug("cacheBasePathDir: " + cacheBasePathDir);
		init();
	}
	
    
	private void init() {
    	 //Set saxon as transformer.  
        System.setProperty("javax.xml.transform.TransformerFactory",  
                           "net.sf.saxon.TransformerFactoryImpl"); 
        Source xsltSource = new StreamSource(xsltPath);
		TransformerFactory transFact = TransformerFactory.newInstance();
		try {
			cachedXSLT = transFact.newTemplates(xsltSource);
			transformer = cachedXSLT.newTransformer();
			URIResolver resolver = (URIResolver) new ClarinProfileResolver(cacheBasePathDir);
			transformer.setURIResolver(resolver);
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by: " + e.getCause());
		} catch (ConverterException e) {
			log.error("ERROR: ConverterException, caused by: " + e.getCause());
		}
		
		
	}
	public void simpleTransform(String xmlSourcePath, String rdfFileOutputName, String base) {  
		log.debug("Converting '" + xmlSourcePath + "' to '" + rdfFileOutputName +"' with base is '" + base + "'" );	
		
		try {
			transformer.setParameter("base", base);
			long start = System.currentTimeMillis();
			transformer.transform(new StreamSource(new File(xmlSourcePath)),  
					 new StreamResult(new File(rdfFileOutputName)));
			long end = System.currentTimeMillis();
			log.info("Duration: " + (end-start) + " milliseconds");
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by: " + e.getCause());
		} catch (TransformerException e) {
			log.error("ERROR: TransformerException, caused by: " + e.getCause());
		}    
    }     
}  