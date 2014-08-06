/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import nl.knaw.dans.clarin.cmd2rdf.exception.ConverterException;

import org.apache.commons.io.FileUtils;
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
	private String registry;
	public ClarinProfileResolver(String basePath, String registry, CacheService<Object, Object> cacheservice) throws ConverterException {
		createCacheTempIfAbsent(basePath);
		this.registry = registry;
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
		if (href.contains("p_1360230992133")) {
			log.info("href: " + href);
			log.info("href size: " + href.length());
			try {
				String decoded = URLDecoder.decode(href, "UTF-8");
				log.info("decode: " + decoded);
				log.info("decode size: " + decoded.length());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.debug("Profile URI: " + href);
		log.debug("xsl base: " + base);
		String filename = href.replace(registry, "");//TODO: Don't use hard coded!!!
		filename = filename.replace("/xml", ".xml");
		filename = filename.replace(":", "_");
	    
	    if (cacheservice.retrieveByteArray(filename) != null) {
	    	log.debug("########## Using profile from the cache: " + href);
	    	byte b[] = (byte[]) cacheservice.retrieveByteArray(filename);
	    	InputStream is = new ByteArrayInputStream(b);
	    	 return new StreamSource(is);
	    }

	    File file = new File(basePath + "/" + filename);
	    if (file.exists()) {
	    	return loadFromFile(filename, file);
	    } else {
	    	return fetchAndWriteToCache(href, filename);
	    }

	}

	private StreamSource fetchAndWriteToCache(String href, String filename) {
		log.debug("========== Download from registry: " + filename);
		URL url;
		final ReadWriteLock rwl = new ReentrantReadWriteLock();
		try {
		    if (href.contains("p_1360230992133")) {
		    	href = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1360230992133/xml";
		    	filename = "clarin.eu_cr1_p_1360230992133.xml";
		    }
		    url = new URL(href);
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(url.openStream()));
			 String inputLine;
			 StringBuffer sb = new StringBuffer();
		        while ((inputLine = in.readLine()) != null)
		            sb.append(inputLine);
		        in.close();
			 byte b[] = sb.toString().getBytes(StandardCharsets.UTF_8);
			 InputStream is = new ByteArrayInputStream(b);
			 cacheservice.putByteArray(filename, b);
			 log.debug(">>>> " + cacheservice.entries() + " put to catche service: " + filename);
			 rwl.writeLock().lock();
			 FileUtils.writeByteArrayToFile(new File(basePath + "/" + filename), b);
			 
			 return new StreamSource(is);
		} catch (MalformedURLException e) {
			log.error("ERROR: Caused by MalformedURLException, msg: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.error("ERROR: Caused by IOException, msg: " + e.getMessage());
			e.printStackTrace();
		}finally {
		  rwl.writeLock().unlock(); // Unlock write
		}
		return null;
	}

	private StreamSource loadFromFile(String filename, File file) {
		StreamSource stream = null;
		final ReadWriteLock rwl = new ReentrantReadWriteLock();
		rwl.readLock().lock();
		log.debug("-----read cache from file and put in the memory cache: " + filename);
		
		try {
			byte[] bytes = FileUtils.readFileToByteArray(file);
			cacheservice.putByteArray(filename, bytes);
			InputStream is = new ByteArrayInputStream(bytes);
			stream = new StreamSource(is);
			return stream; 
		} catch (IOException e) {
			log.error("FATAL ERROR: could not put the profile to the cache. Caused by IOException, msg: " + e.getMessage());
		}  finally {
		      rwl.readLock().unlock(); //Unlock read
		}
		return stream;
	}

}
