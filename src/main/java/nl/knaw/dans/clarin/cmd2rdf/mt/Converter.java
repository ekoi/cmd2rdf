package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import nl.knaw.dans.clarin.cmd2rdf.exception.ConverterException;

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
	private String cacheBasePathDir;
	private Templates cachedXSLT;
	
	
	public Converter( String xmlSourcePathDir,  String cacheBasePathDir,  Templates cachedXSLT){
		this.xmlSourcePathDir = xmlSourcePathDir;
		this.cacheBasePathDir = cacheBasePathDir;
		this.cachedXSLT = cachedXSLT;
		
	}
	
	public Converter( String xmlSourcePathDir, String xsltPath, String cacheBasePathDir){
		this.xmlSourcePathDir = xmlSourcePathDir;
		this.cacheBasePathDir = cacheBasePathDir;
		log.debug("xsltPath: " + xsltPath);
		log.debug("cacheBasePathDir: " + cacheBasePathDir);
		TransformerFactory transFact = new net.sf.saxon.TransformerFactoryImpl();
		Source xsltSource = new StreamSource(xsltPath);
		try {
			this.cachedXSLT = transFact.newTemplates(xsltSource);
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by: " + e.getMessage());
		}
		
	}

	public ByteArrayOutputStream simpleTransform(String xmlSourcePath, String rdfFileOutputName, String baseURI, String registry,CacheService<Object, Object> cacheservice) {  
		ByteArrayOutputStream bos=null;
		try {
			log.debug("Converting '" + xmlSourcePath + "' to '" + rdfFileOutputName +"' with base is '" + baseURI + "'" );
			URIResolver resolver = (URIResolver) new ClarinProfileResolver(cacheBasePathDir, registry, cacheservice);
			Transformer transformer = cachedXSLT.newTransformer();	
			transformer.setURIResolver(resolver);
			transformer.setParameter("base_strip", "file:" + xmlSourcePathDir);
			transformer.setParameter("base_add", baseURI);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			long start = System.currentTimeMillis();
			bos=new ByteArrayOutputStream();
			 StreamResult result=new StreamResult(bos);
			transformer.transform(new StreamSource(new File(xmlSourcePath)),  
					 result);
			long end = System.currentTimeMillis();
			log.info("Duration of transformation of " + rdfFileOutputName + " : " + ((end-start)) + " milliseconds");
			
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by: " + e.getCause());
		} catch (TransformerException e) {
			log.error("ERROR: TransformerException, caused by: " + e.getCause());
		} catch (ConverterException e) {
			log.error("ERROR: ConverterException, caused by: " + e.getCause());
		}
		return bos;    
    }     
}  