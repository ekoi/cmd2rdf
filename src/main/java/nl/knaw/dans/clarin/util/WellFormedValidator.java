package nl.knaw.dans.clarin.util;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.omg.CORBA.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class WellFormedValidator {
	private static final Logger log = LoggerFactory.getLogger(WellFormedValidator.class);

	/**
	   * This is parsing code
	   *
	   * @param xml The input argument to check.
	   * @throws SAXException
	   *             If the input xml is invalid
	   *
	   * @throws SystemException
	   *             Thrown if the input string cannot be read
	   */
	
	  public static void validate(String xml) throws SAXException, IOException, ParserConfigurationException {
		  SAXParserFactory factory = SAXParserFactory.newInstance();
		  factory.setValidating(false);
		  factory.setNamespaceAware(true);

		  SAXParser parser = factory.newSAXParser();

		  XMLReader reader = parser.getXMLReader();
		  reader.setErrorHandler(new SimpleErrorHandler());
		  reader.parse(new InputSource(xml));
	  }
	  
	  public static void main(String args[]) {
		  try {
			validate("/Users/akmi/git/cmd2rdf/src/test/data/cmd-rdf/oai_beeldengeluid_nl_Expressie_1000278.rdf");
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }

	}
