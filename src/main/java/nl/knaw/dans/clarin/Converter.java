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
	
	private static final Logger logger = LoggerFactory.getLogger(Converter.class);
	
	public Converter(){
		init();
	}
    private void init() {
    	 //Set saxon as transformer.  
        System.setProperty("javax.xml.transform.TransformerFactory",  
                           "net.sf.saxon.TransformerFactoryImpl"); 
		
	}
	public void simpleTransform(String sourcePath, String xsltPath,  
                                       String resultDir) {  
		logger.debug("sourcePath: " + sourcePath);
		logger.debug("xsltPath: " + xsltPath);
		logger.debug("resultDir: " + resultDir);
//        TransformerFactory tFactory = TransformerFactory.newInstance();  
//        try {  
//            Transformer transformer =  
//                tFactory.newTransformer(new StreamSource(new File(xsltPath)));  
//  
//            transformer.transform(new StreamSource(new File(sourcePath)),  
//                                  new StreamResult(new File(resultDir)));  
//        } catch (Exception e) {  
//            e.printStackTrace();  
//        }  
		
		Source xsltSource = new StreamSource(xsltPath);
		TransformerFactory transFact = TransformerFactory.newInstance();
		Templates cachedXSLT;
		try {
			cachedXSLT = transFact.newTemplates(xsltSource);
			Transformer trans = cachedXSLT.newTransformer();
			URIResolver resolver = (URIResolver) new ClarinProfileResolver("/tmp");
			trans.setURIResolver(resolver);
			trans.setParameter("base","http://localhost:8000/DV");
			trans.transform(new StreamSource(new File(sourcePath)),  
					 new StreamResult(new File(resultDir)));
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ConverterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

     
    }  
  
    public static void main(String[] args) {  
    	logger.trace("args" + args.toString());
    	Converter c = new Converter();
    	c.simpleTransform("src/test/data/cmd-xml/oai_beeldengeluid_nl_Expressie_1000278.xml", 
    			"src/main/resources/xsl/CMDRecord2RDF.xsl", "src/test/data/out/oai_beeldengeluid_nl_Expressie_1000278.rdf");
  
//    	c.simpleTransform("src/test/data/data.xml", 
//    			"src/test/data/eko.xsl", "src/test/data/out/oai_beeldengeluid_nl_Expressie_1000278.rdf");
//  
    }  
}  
