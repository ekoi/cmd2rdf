/**
 * 
 */
package nl.knaw.dans.clarin.mt;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import nl.knaw.dans.clarin.ConverterException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Eko Indarto
 *
 */
public class ClarinProfileResolver implements URIResolver {
	private static final Logger log = LoggerFactory.getLogger(ClarinProfileResolver.class);
	private String basePath;
	private List<String> profilesList;
	public ClarinProfileResolver(String basePath, List<String> profilesList) throws ConverterException {
		createCacheTempIfAbsent(basePath);
		this.basePath = basePath;
		this.profilesList = profilesList;
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
		filename = basePath + "/" + filename;
	    
	    synchronized (profilesList) {
	    	File file = new File(filename);
		    if (!profilesList.contains(filename)) {
		    	profilesList.add(filename);
		    	if (!file.exists()) {
			    	profilesList.add(filename);
			    	final ReadWriteLock rwl = new ReentrantReadWriteLock();
		            rwl.writeLock().lock();
		            rwl.readLock().lock();
		            try {
			    		log.debug("===Save=== " + href + " to file: " + filename);
			    		FileUtils.copyURLToFile(new URL(href), file);
					} catch (MalformedURLException e) {
						log.error("Error during caching for " + href + ", caused by: " + e.getMessage());
					} catch (IOException e) {
						log.error("Error during caching for " + href + ", caused by: " + e.getMessage());
					}finally {
				         
				          rwl.readLock().unlock(); //Unlock read
				          rwl.writeLock().unlock(); // Unlock write
			        }
		    	}
	            return new StreamSource(file);
		    } else {
		    	log.debug("##########Using profile from the cache: " + href);
		    	return new StreamSource(file);
		    }
	  }
	}

}
