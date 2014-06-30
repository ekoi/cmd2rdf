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

import org.apache.commons.io.FileUtils;
import org.apache.directmemory.DirectMemory;
import org.apache.directmemory.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author akmi
 *
 */
public class ThreadPoolConverter {
	
	//private static List<String> profilesList = Collections.synchronizedList(new ArrayList<String>());
	//final BlockingQueue<File> bq = new ArrayBlockingQueue<File>(26);
	private static final Logger log = LoggerFactory.getLogger(ThreadPoolConverter.class);
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
	 
	public ThreadPoolConverter(){
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
    	
    	
    		execute(xmlSrcPathDir, xsltPath, rdfOutputDir, baseURI,
				cacheBasePathDir, registry, lf, Integer.parseInt(nThreads));
    
    }

	
	private static void execute(String xmlSourcePathDir, String xsltPath,
			String rdfOutpuDir, String baseURI, String cacheBasePathDir, 
			String registry, List<File> files, int nThreads) {
	    System.out.println(nThreads);
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		log.info("@@@ begin of execution, size: " + files.size() );
    	 for (File file : files) {
    		 
             Runnable worker = new WorkerThread(file, xmlSourcePathDir, baseURI, 
            		 					rdfOutpuDir, xsltPath, cacheBasePathDir, registry, cacheService);
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
    
    
    

}
