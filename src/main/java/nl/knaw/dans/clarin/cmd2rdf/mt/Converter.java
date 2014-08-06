package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

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

import org.apache.directmemory.DirectMemory;
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
	private static final CacheService<Object, Object> cacheService = new DirectMemory<Object, Object>()
		    .setNumberOfBuffers( 75 )
		    .setSize( 1000000 )
		    .setInitialCapacity( 10000 )
		    .setConcurrencyLevel( 4 )
		    .newCacheService();
	private Templates cachedXSLT;
	private String xmlSrcPathDir;
	private String cacheBasePathDir;
	private String registry;
	private String baseURI;
	
	
	public Converter( String xmlSrcPathDir, String xsltPath, String cacheBasePathDir, String registry, String baseURI){
		this.xmlSrcPathDir = xmlSrcPathDir;
		this.cacheBasePathDir = cacheBasePathDir;
		this.registry = registry;
		this.baseURI = baseURI;
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

	public ByteArrayOutputStream simpleTransform(File file) {  
		ByteArrayOutputStream bos=null;
		try {
			log.debug("Converting '" + file.getAbsolutePath() + "' with base is '" + baseURI + "'" );
			URIResolver resolver = (URIResolver) new ClarinProfileResolver(cacheBasePathDir, registry, cacheService);
			Transformer transformer = cachedXSLT.newTransformer();	
			transformer.setURIResolver(resolver);
			transformer.setParameter("base_strip", "file:" + xmlSrcPathDir);
			transformer.setParameter("base_add", baseURI);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			long start = System.currentTimeMillis();
			log.debug(">>>>>>>>>>>>>> DO BOS");
			bos=new ByteArrayOutputStream();
			 StreamResult result=new StreamResult(bos);
			 log.debug(">>>>>>>>>>>>>> DO xmlInput");
			StreamSource xmlInput = new StreamSource(file);
			log.debug(">>>>>>>>>>>>>> DO TRANSFORM");
			transformer.transform(xmlInput,  
					 result);
			long end = System.currentTimeMillis();
			log.debug("<<<<<<<<<<<<<< TRANSFORM IS DONE");
			log.info("Duration of transformation of " + file.getAbsolutePath() + " : " + ((end-start)) + " milliseconds");
			
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by: " + e.getCause());
		} catch (TransformerException e) {
			log.error("ERROR: TransformerException, caused by: " + e.getCause());
		} catch (ConverterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return bos;    
    }     
	
	public void shutDown() {
		cacheService.clear();
        try {
			cacheService.close();
		} catch (IOException e) {
			log.error("ERROR caused by IOException, msg:  " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}  