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
 * Hello world!
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
	
	public Converter(String xsltPath, String cacheBasePathDir){
		this.xsltPath = xsltPath;
		this.cacheBasePathDir = cacheBasePathDir;
		log.debug("xsltPath: " + xsltPath);
		log.debug("cacheBasePathDir: " + cacheBasePathDir);
		init();
	}
	
//	public Converter(){
//		init();
//	}
    
	private void init() {
    	 //Set saxon as transformer.  
        System.setProperty("javax.xml.transform.TransformerFactory",  
                           "net.sf.saxon.TransformerFactoryImpl"); 
		
	}
	public void simpleTransform(String xmlSourcePath, String rdfFileOutputName, String base) {  
		log.debug("Converting '" + xmlSourcePath + "' to '" + rdfFileOutputName +"' with base is '" + base + "'" );	
		Source xsltSource = new StreamSource(xsltPath);
		TransformerFactory transFact = TransformerFactory.newInstance();
		Templates cachedXSLT;
		try {
			cachedXSLT = transFact.newTemplates(xsltSource);
			Transformer trans = cachedXSLT.newTransformer();
			URIResolver resolver = (URIResolver) new ClarinProfileResolver(cacheBasePathDir);
			trans.setURIResolver(resolver);
			trans.setParameter("base", base);
			trans.transform(new StreamSource(new File(xmlSourcePath)),  
					 new StreamResult(new File(rdfFileOutputName)));
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ConverterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     
    }    
}  
