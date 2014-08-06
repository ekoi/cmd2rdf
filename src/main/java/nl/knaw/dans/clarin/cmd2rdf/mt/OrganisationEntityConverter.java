/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akmi
 *
 */
public class OrganisationEntityConverter implements Converter {

	private static final Logger log = LoggerFactory.getLogger(OrganisationEntityConverter.class);
	private Templates cachedXSLT;
	
	public OrganisationEntityConverter(String xsltPath) {
		TransformerFactory transFact = new net.sf.saxon.TransformerFactoryImpl();
		Source xsltSource = new StreamSource(xsltPath);
		try {
			this.cachedXSLT = transFact.newTemplates(xsltSource);
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by: " + e.getMessage());
		}
	}

	public ByteArrayOutputStream transform(Object object) {
		ByteArrayOutputStream bosInput = (ByteArrayOutputStream)object;
		ByteArrayOutputStream bos=null;
		try {
			Transformer transformer = cachedXSLT.newTransformer();	
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setParameter("VLO-orgs", "file:/Users/akmi/Dropbox/DANS/IN_PROGRESS/CMDI2RDF-Workspace/tmp/meertens-VLO-orgs.rdf");
			long start = System.currentTimeMillis();
			log.debug(">>>>>>>>>>>>>> DO BOS");
			bos=new ByteArrayOutputStream();
			 StreamResult result=new StreamResult(bos);
			 log.debug(">>>>>>>>>>>>>> DO xmlInput");
			 
			 InputStream decodedInput=new ByteArrayInputStream(bosInput.toByteArray());
			StreamSource xmlInput = new StreamSource(decodedInput);
			log.debug(">>>>>>>>>>>>>> DO TRANSFORM");
			transformer.transform(xmlInput,  
					 result);
			long end = System.currentTimeMillis();
			log.debug("<<<<<<<<<<<<<< TRANSFORM IS DONE");
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by: " + e.getCause());
		} catch (TransformerException e) {
			log.error("ERROR: TransformerException, caused by: " + e.getCause());
		} 
		return bos;   
	}

}
