package nl.knaw.dans.clarin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SimpleErrorHandler implements ErrorHandler {
	
	private static final Logger log = LoggerFactory.getLogger(SimpleErrorHandler.class);
	public void warning(SAXParseException e) throws SAXException {
		log.warn(e.getMessage());
    }

    public void error(SAXParseException e) throws SAXException {
        log.error(e.getMessage());
    }

    public void fatalError(SAXParseException e) throws SAXException {
    	log.error(e.getMessage());
    }

}
