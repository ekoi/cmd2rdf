package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.File;
import java.util.List;

import nl.knaw.dans.clarin.cmd2rdf.util.WellFormedValidator;

import org.apache.directmemory.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerThread implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(WorkerThread.class);
    private List<File> subSets;
	private String xmlSourcePathDir;
	private String baseURI;
	private String rdfOutpuDir;
	private Converter converter;
	private CacheService<Object, Object> cacheservice;

    public WorkerThread(List<File> subSets, String xmlSourcePathDir, String baseURI, String rdfOutpuDir
    					, String xsltPath, String cacheBasePathDir, CacheService<Object, Object> cacheservice){
        this.subSets = subSets;
        this.xmlSourcePathDir = xmlSourcePathDir;
        this.baseURI = baseURI;
        this.rdfOutpuDir = rdfOutpuDir;
        this.cacheservice = cacheservice;
        converter = new Converter(xmlSourcePathDir, xsltPath, cacheBasePathDir);
    }

    public void run() {

    	log.debug("=== run ===");
        processTransformation();

    }
    
    private void processTransformation() {
		for (File file: subSets) {
	    	String relativeFilePath =  file.getAbsolutePath().replace(xmlSourcePathDir, "").replace(".xml", ".rdf");
			String rdfOutputPath = rdfOutpuDir + relativeFilePath;
			converter.simpleTransform(file.getAbsolutePath(), rdfOutputPath, baseURI, cacheservice);
			boolean validRdf = WellFormedValidator.validate(rdfOutputPath);
			if (!validRdf) {
				log.info("INVALID RDF: "+ rdfOutputPath);
			} 
      
    	}

    }

 
}
