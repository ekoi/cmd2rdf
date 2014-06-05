/**
 * 
 */
package nl.knaw.dans.clarin.mt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import nl.knaw.dans.clarin.ConverterException;

import org.apache.directmemory.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Eko Indarto
 *
 */
public class ClarinProfileResolver implements URIResolver {
	private static final Logger log = LoggerFactory.getLogger(ClarinProfileResolver.class);
	private String basePath;
	private CacheService<Object, Object> cacheservice;
	public ClarinProfileResolver(String basePath, CacheService<Object, Object> cacheservice) throws ConverterException {
		createCacheTempIfAbsent(basePath);
		this.basePath = basePath;
		this.cacheservice = cacheservice;
	}


	private boolean createCacheTempIfAbsent(String basePath) throws ConverterException {
		boolean success = false;
		File dir = new File(basePath);
		if (!dir.exists()) {
			success = dir.mkdir();
			if (!success)
				throw new ConverterException("ERROR: Cannot create cache directory '" + basePath + "'.");
			else
				log.info("Cache directory is created: " + basePath);
		}
		return success;
	}

	  
	public Source resolve(String href,String base) throws TransformerException {
		log.debug("Profile URI: " + href);
		log.debug("xsl base: " + base);
		String filename = href.replace("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/", "");//TODO: Don't use hard coded!!!
		filename = filename.replace("/xml", ".xml");
		filename = filename.replace(":", "_");
		//filename = basePath + "/" + filename;
	    
	    synchronized (cacheservice) {
		    if (cacheservice.retrieveByteArray(filename) == null) {
		    	log.debug("==========Download from registry");
		    	URL oracle;
				try {
					oracle = new URL(href);
					BufferedReader in = new BufferedReader(
					        new InputStreamReader(oracle.openStream()));
					 String inputLine;
					 StringBuffer sb = new StringBuffer();
				        while ((inputLine = in.readLine()) != null)
				            sb.append(inputLine);
				        in.close();
					 byte b[] = sb.toString().getBytes(StandardCharsets.UTF_8);
					 InputStream is = new ByteArrayInputStream(b);
					 cacheservice.putByteArray(filename, b);
					 return new StreamSource(is);
				} catch (MalformedURLException e) {
					log.error("ERROR: Caused by MalformedURLException, msg: " + e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					log.error("ERROR: Caused by IOException, msg: " + e.getMessage());
					e.printStackTrace();
				}
		    } else {
		    	log.debug("##########Using profile from the cache: " + href);
		    	byte b[] = (byte[]) cacheservice.retrieveByteArray(filename);
		    	InputStream is = new ByteArrayInputStream(b);
		    	 return new StreamSource(is);
		    }
	  }
	    log.error("ERROR: THIS IS SHOULD NEVER HAPPENED");
		return null;
	}

}
