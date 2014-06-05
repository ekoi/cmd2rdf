package nl.knaw.dans.clarin.mt;

import java.io.File;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import nl.knaw.dans.clarin.ConverterException;

import org.apache.directmemory.cache.CacheService;
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
	private  String xmlSourcePathDir;
	private String xsltPath;
	private String cacheBasePathDir;
	
	public Converter( String xmlSourcePathDir, String xsltPath, String cacheBasePathDir){
		this.xmlSourcePathDir = xmlSourcePathDir;
		this.xsltPath = xsltPath;
		this.cacheBasePathDir = cacheBasePathDir;
		log.debug("xsltPath: " + xsltPath);
		log.debug("cacheBasePathDir: " + cacheBasePathDir);
		//init();
	}
	
    
	private void init() {
    	 //Set saxon as transformer.  
        System.setProperty("javax.xml.transform.TransformerFactory",  
                           "net.sf.saxon.TransformerFactoryImpl"); 
	}
	public void simpleTransform(String xmlSourcePath, String rdfFileOutputName, String baseURI, CacheService<Object, Object> cacheservice) {  
		log.debug("Converting '" + xmlSourcePath + "' to '" + rdfFileOutputName +"' with base is '" + baseURI + "'" );	
		TransformerFactory transFact = TransformerFactory.newInstance();
		try {
			URIResolver resolver = (URIResolver) new ClarinProfileResolver(cacheBasePathDir, cacheservice);
			Transformer transformer = transFact.newTransformer(new StreamSource(new File(xsltPath)));	
			transformer.setURIResolver(resolver);
			transformer.setParameter("base_strip", "file:" + xmlSourcePathDir);
			transformer.setParameter("base_add", baseURI);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			long start = System.currentTimeMillis();
			transformer.transform(new StreamSource(new File(xmlSourcePath)),  
					 new StreamResult(new File(rdfFileOutputName)));
			long end = System.currentTimeMillis();
			log.info("Duration: " + (end-start) + " milliseconds");
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by: " + e.getCause());
		} catch (TransformerException e) {
			log.error("ERROR: TransformerException, caused by: " + e.getCause());
		} catch (ConverterException e) {
			log.error("ERROR: ConverterException, caused by: " + e.getCause());
		}    
    }     
}  