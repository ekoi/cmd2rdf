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
public class XsltTransformer implements IAction{
    /** 
     * Simple transformation method. 
     * @param sourcePath - Absolute path to source xml file. 
     * @param xsltPath - Absolute path to xslt file. 
     * @param resultDir - Directory where you want to put resulting files. 
     */  
	
	private static final Logger log = LoggerFactory.getLogger(XsltTransformer.class);
	private static CacheService<Object, Object> cacheService;
	private Templates cachedXSLT;
	private String xsltSource;
	private String profilesCacheDir;
	private String registry;
	private String baseStripParam;
	private Object baseURIParam;
	
	
	public XsltTransformer(){
		
	}
	
	public void startUp() throws ConverterException{
		log.debug("XmlToRdfConverter variables: ");
		log.debug("xsltSource: " + xsltSource);
		log.debug("profilesCacheDir: " + profilesCacheDir);
		log.debug("registry: " + registry);
		log.debug("baseStripParam: " + baseStripParam);
		log.debug("baseURIParam: " + baseURIParam);
		log.debug("Start XmlToRdfConverter....");
		checkRequiredVariables();
		startUpCacheService();
		TransformerFactory transFact = new net.sf.saxon.TransformerFactoryImpl();
		Source src = new StreamSource(xsltSource);
		try {
			this.cachedXSLT = transFact.newTemplates(src);
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by: " + e.getMessage());
		}
	}

	private void checkRequiredVariables() throws ConverterException {
		if (xsltSource == null || xsltSource.isEmpty())
			throw new ConverterException("xsltPath is null or empty");
		
		if (profilesCacheDir == null || profilesCacheDir.isEmpty())
			throw new ConverterException("cacheBasePathDir is null or empty");
		
		if (registry == null || registry.isEmpty())
			throw new ConverterException("registry is null or empty");
		
//		if (baseURI == null || baseURI.isEmpty())
//			throw new ConverterException("baseURI is null or empty");
	}
	
	private void startUpCacheService() {
		cacheService = new DirectMemory<Object, Object>()
			    .setNumberOfBuffers( 75 )
			    .setSize( 1000000 )
			    .setInitialCapacity( 10000 )
			    .setConcurrencyLevel( 4 )
			    .newCacheService();
	}
	
	public ByteArrayOutputStream execute(Object o) {
		File file = (File)o;
		ByteArrayOutputStream bos=null;
		try {
			//log.debug("Converting '" + file.getAbsolutePath() + "' with base is '" + baseURI + "'" );
			URIResolver resolver = (URIResolver) new ClarinProfileResolver(profilesCacheDir, registry, cacheService);
			Transformer transformer = cachedXSLT.newTransformer();	
			transformer.setURIResolver(resolver);
			transformer.setParameter("base_strip", "file:" + baseStripParam);
			transformer.setParameter("base_add", baseURIParam);
			//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			long start = System.currentTimeMillis();
			bos=new ByteArrayOutputStream();
			StreamResult result=new StreamResult(bos);
			StreamSource xmlInput = new StreamSource(file);
			transformer.transform(xmlInput,  
					 result);
			long end = System.currentTimeMillis();
			log.info("Duration of transformation of " + file.getAbsolutePath() + " : " + ((end-start)) + " milliseconds");
			
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by: " + e.getCause());
		} catch (TransformerException e) {
			log.error("ERROR: TransformerException, caused by: " + e.getCause());
		} catch (ConverterException e) {
			log.error("ERROR: ConverterException, caused by: " + e.getCause());
		} 
		return bos;    
    }     
	
	public void shutDownCacheService() {
		cacheService.clear();
        try {
			cacheService.close();
		} catch (IOException e) {
			log.error("ERROR caused by IOException, msg:  " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void shutDown() throws ConverterException {
		shutDownCacheService();
	}
}  