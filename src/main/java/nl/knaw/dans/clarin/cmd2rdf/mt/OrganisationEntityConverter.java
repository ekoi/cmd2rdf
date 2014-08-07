/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
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
	private String xsltPath;
	private String vloOrgsParam;
	
	public OrganisationEntityConverter() {
	}
	
	public void startUp() throws ConverterException {
		checkRequiredVariables();
		
		log.debug("xsltPath: " + xsltPath);
		log.debug("vloOrgsParam: " + vloOrgsParam);
		
		TransformerFactory transFact = new net.sf.saxon.TransformerFactoryImpl();
		Source xsltSource = new StreamSource(xsltPath);
		try {
			this.cachedXSLT = transFact.newTemplates(xsltSource);
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by: " + e.getMessage());
		}
	}

	private void checkRequiredVariables() throws ConverterException {
		if (getXsltPath() == null || getXsltPath().isEmpty())
			throw new ConverterException("xsltPath is null or empty");
		
		if (getVloOrgsParam() == null || getVloOrgsParam().isEmpty())
			throw new ConverterException("vloOrgsParam is null or empty");
	}

	public ByteArrayOutputStream transform(Object object) {
		ByteArrayOutputStream bosInput = (ByteArrayOutputStream)object;
		ByteArrayOutputStream bos=null;
		try {
			Transformer transformer = cachedXSLT.newTransformer();	
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setParameter("VLO-orgs", "file:" + getVloOrgsParam());
			long start = System.currentTimeMillis();
			bos=new ByteArrayOutputStream();
			 StreamResult result=new StreamResult(bos);
			 
			 InputStream decodedInput=new ByteArrayInputStream(bosInput.toByteArray());
			StreamSource xmlInput = new StreamSource(decodedInput);
			transformer.transform(xmlInput,  
					 result);
			long end = System.currentTimeMillis();
			log.debug("Duration of OrganisationEntityConverter: " + (end-start) + " milliseconds");
		} catch (TransformerConfigurationException e) {
			log.error("ERROR: TransformerConfigurationException, caused by: " + e.getCause());
		} catch (TransformerException e) {
			log.error("ERROR: TransformerException, caused by: " + e.getCause());
		} 
		return bos;   
	}

	public String getXsltPath() {
		return xsltPath;
	}

	public void setXsltPath(String xsltPath) {
		this.xsltPath = xsltPath;
	}

	public String getVloOrgsParam() {
		return vloOrgsParam;
	}

	public void setVloOrgsParam(String vloOrgsParam) {
		this.vloOrgsParam = vloOrgsParam;
	}

	

}
