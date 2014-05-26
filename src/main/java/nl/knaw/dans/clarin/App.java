package nl.knaw.dans.clarin;

import java.io.File;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Hello world!
 *
 */
public class App {
    /** 
     * Simple transformation method. 
     * @param sourcePath - Absolute path to source xml file. 
     * @param xsltPath - Absolute path to xslt file. 
     * @param resultDir - Directory where you want to put resulting files. 
     */  
    public static void simpleTransform(String sourcePath, String xsltPath,  
                                       String resultDir) {  
        TransformerFactory tFactory = TransformerFactory.newInstance();  
        try {  
            Transformer transformer =  
                tFactory.newTransformer(new StreamSource(new File(xsltPath)));  
  
            transformer.transform(new StreamSource(new File(sourcePath)),  
                                  new StreamResult(new File(resultDir)));  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
  
    public static void main(String[] args) {  
        //Set saxon as transformer.  
        System.setProperty("javax.xml.transform.TransformerFactory",  
                           "net.sf.saxon.TransformerFactoryImpl");  
  
        simpleTransform("/Users/akmi/CMDI2RDF-Workspace/CMD2RDF-SVN/CMD2RDF/trunk/data/cmd-xml/oai_beeldengeluid_nl_Expressie_1000278.xml",  
                        "/Users/akmi/CMDI2RDF-Workspace/CMD2RDF-SVN/CMD2RDF/trunk/xsl/CMD2RDF.xsl", "/Users/akmi/CMDI2RDF-Workspace/tmp/oai_beeldengeluid_nl_Expressie_1000278.rdf");  
  
    }  
}  
