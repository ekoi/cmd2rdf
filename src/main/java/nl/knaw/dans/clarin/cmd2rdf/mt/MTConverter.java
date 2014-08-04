/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.knaw.dans.clarin.cmd2rdf.util.Misc;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.directmemory.DirectMemory;
import org.apache.directmemory.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Eko Indarto
 *
 */
public class MTConverter {
	
	//private static List<String> profilesList = Collections.synchronizedList(new ArrayList<String>());
	//final BlockingQueue<File> bq = new ArrayBlockingQueue<File>(26);
	private static final Logger log = LoggerFactory.getLogger(MTConverter.class);
	private static final CacheService<Object, Object> cacheService = new DirectMemory<Object, Object>()
		    .setNumberOfBuffers( 75 )
		    .setSize( 1000000 )
		    .setInitialCapacity( 10000 )
		    .setConcurrencyLevel( 4 )
		    .newCacheService();
	
	private String xmlSrcPathDir;
	private String xsltPath;
	private String rdfOutputDir;
	private String baseURI;
	private String cacheBasePathDir;
	private String nThreads;
	private String registry;
	private String virtuosoUrl;
	private String virtuosoUser;
	private String virtuosoPass;
	 
	public MTConverter(){
	}
	
	public void process() {  
		System.out.println(cacheBasePathDir);
    	log.debug("===== Collect list of files.======");
    	Collection<File> listFiles = FileUtils.listFiles(new File(xmlSrcPathDir),new String[] {"xml"}, true);
    	List<Map.Entry> shortedMap = Misc.shortedBySize(listFiles);
    	
    	List<File> lf = new ArrayList<File>();
    	for (Map.Entry e : shortedMap) {
    	       lf.add(new File((String) e.getKey()));
    	}
    	
    	log.debug("===== Multithreading Processing " + lf.size() + " list of files.======");
    	
//    	 List<List<File>> subSets = ListUtils.partition(lf, Integer.parseInt(nThreads));
// 	    for (List<File> fs : subSets) {
//    		execute(xmlSrcPathDir, xsltPath, rdfOutputDir, baseURI,
//				cacheBasePathDir, registry, fs, Integer.parseInt(nThreads));
// 	    }
    	
    	execute(xmlSrcPathDir, xsltPath, rdfOutputDir, baseURI,
				cacheBasePathDir, registry, lf, Integer.parseInt(nThreads));
    
    }

	
	private void execute(String xmlSourcePathDir, String xsltPath,
			String rdfOutpuDir, String baseURI, String cacheBasePathDir, 
			String registry, List<File> files, int nThreads) {
	    System.out.println(nThreads);
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		log.info("@@@ begin of execution, size: " + files.size() );
    	 for (File file : files) {
    		 
             Runnable worker = new WorkerThread(file, xmlSourcePathDir, baseURI, 
            		 					rdfOutpuDir, xsltPath, cacheBasePathDir, registry, cacheService, virtuosoUrl, virtuosoUser, virtuosoPass);
             executor.execute(worker);
           }
         executor.shutdown();
         
         while (!executor.isTerminated()) {}
         
         log.info("Finished all threads");
         cacheService.clear();
         try {
			cacheService.close();
		} catch (IOException e) {
			log.error("ERROR caused by IOException, msg:  " + e.getMessage());
			e.printStackTrace();
		}
	}

	public String getXmlSrcPathDir() {
		return xmlSrcPathDir;
	}

	public void setXmlSrcPathDir(String xmlSrcPathDir) {
		this.xmlSrcPathDir = xmlSrcPathDir;
	}

	public String getXsltPath() {
		return xsltPath;
	}

	public void setXsltPath(String xsltPath) {
		this.xsltPath = xsltPath;
	}

	public String getRdfOutputDir() {
		return rdfOutputDir;
	}

	public void setRdfOutputDir(String rdfOutputDir) {
		this.rdfOutputDir = rdfOutputDir;
	}

	public String getBaseURI() {
		return baseURI;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public String getCacheBasePathDir() {
		return cacheBasePathDir;
	}

	public void setCacheBasePathDir(String cacheBasePathDir) {
		this.cacheBasePathDir = cacheBasePathDir;
	}

	public String getnThreads() {
		return nThreads;
	}

	public void setnThreads(String nThreads) {
		this.nThreads = nThreads;
	}

	public String getRegistry() {
		return registry;
	}

	public void setRegistry(String registry) {
		this.registry = registry;
	}

	public String getVirtuosoUrl() {
		return virtuosoUrl;
	}

	public void setVirtuosoUrl(String virtuosoUrl) {
		this.virtuosoUrl = virtuosoUrl;
	}

	public String getVirtuosoUser() {
		return virtuosoUser;
	}

	public void setVirtuosoUser(String virtuosoUser) {
		this.virtuosoUser = virtuosoUser;
	}

	public String getVirtuosoPass() {
		return virtuosoPass;
	}

	public void setVirtuosoPass(String virtuosoPass) {
		this.virtuosoPass = virtuosoPass;
	}  
    
    
    

}
