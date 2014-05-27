/**
 * 
 */
package nl.knaw.dans.clarin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akmi
 *
 */
public class ClarinProfileResolver implements URIResolver {
	private static final Logger log = LoggerFactory.getLogger(ClarinProfileResolver.class);
	private String basePath;
	
	public ClarinProfileResolver(String basePath) throws ConverterException {
		File dir = new File(basePath);
		if (!dir.exists())
			throw new ConverterException("Error");
		this.basePath = basePath;
		
	}

	  
	public Source resolve(String href,String base){
		log.debug("Profile URI: " + href);
		log.debug("xsl base: " + base);
		//http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1288172614023/xml
		String filename = href.replace("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/", "");
		filename = filename.replace("/xml", ".xml");
		filename = filename.replace(":", "_");
		filename = basePath + "/" + filename;
	    File file = new File(filename);
	    if(file.exists()) {
	    	log.debug("Using profile from the cache: " + href);
	    	return new StreamSource(file);
	    } else {
	    	try {
	    		log.debug("Save " + href + " to file: " + filename);
				FileUtils.copyURLToFile(new URL(href),file);
			} catch (MalformedURLException e) {
				log.error("Error during caching for " + href + " message: " + e.getMessage());
			} catch (IOException e) {
				log.error("Error during caching for " + href + " message: " + e.getMessage());
			}
	    }
	    return null;
	  }

}
