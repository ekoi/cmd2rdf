package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import nl.knaw.dans.clarin.cmd2rdf.store.VirtuosoStore;
import nl.knaw.dans.clarin.cmd2rdf.util.WellFormedValidator;

import org.apache.directmemory.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerThread implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(WorkerThread.class);
    private File file;
	private String xmlSourcePathDir;
	private String baseURI;
	private String rdfOutpuDir;
	private Converter converter;
	private CacheService<Object, Object> cacheservice;
	private VirtuosoStore vs;
	private String registry;

    public WorkerThread(File file, String xmlSourcePathDir, String baseURI, String rdfOutpuDir
    					, String xsltPath, String cacheBasePathDir,CacheService<Object, Object> cacheservice){
        this.file = file;
        this.xmlSourcePathDir = xmlSourcePathDir;
        this.baseURI = baseURI;
        this.rdfOutpuDir = rdfOutpuDir;
        this.cacheservice = cacheservice;
        this.registry = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/";
        converter = new Converter(xmlSourcePathDir, xsltPath, cacheBasePathDir);
        vs = new VirtuosoStore("http://localhost:8000/sparql-graph-crud-auth", "dba", "dba");
    }
    
    public WorkerThread(File file, String xmlSourcePathDir, String baseURI, String rdfOutpuDir
			, String xsltPath, String cacheBasePathDir, String registry, CacheService<Object, Object> cacheservice){
		this.file = file;
		this.xmlSourcePathDir = xmlSourcePathDir;
		this.baseURI = baseURI;
		this.rdfOutpuDir = rdfOutpuDir;
		this.cacheservice = cacheservice;
		this.registry = registry;
		converter = new Converter(xmlSourcePathDir, xsltPath, cacheBasePathDir);
		vs = new VirtuosoStore("http://localhost:8000/sparql-graph-crud-auth", "dba", "dba");
}

    public void run() {

    	log.debug("=== run ===");
        processTransformation(file);

    }
    
    private void processTransformation(File file) {
		//for (File file: subSets) {
			String rdfFilename =  file.getAbsolutePath().replace(".xml", ".rdf");
	    	String relativeFilePath = rdfFilename.replace(xmlSourcePathDir, "");
			String rdfOutputPath = rdfOutpuDir + relativeFilePath;
			log.debug(file.getName() + " has size of " + file.length() + " bytes (" + (file.length()/1024) + " MB).");
			long start = System.currentTimeMillis();
			ByteArrayOutputStream bos = converter.simpleTransform(file.getAbsolutePath(), rdfOutputPath, baseURI, registry, cacheservice);
			long endConv = System.currentTimeMillis();
			log.info("Duration of Conversion: " + ((endConv-start)/1000) + " seconds");
			String gIri = rdfFilename.replace(xmlSourcePathDir, baseURI);
			gIri = gIri.replace(".xml", ".rdf");
			log.debug("===Upload to Virtuoso, Graph IRI: " + gIri + ". Size: " + bos.size());
			boolean b = vs.save(bos.toByteArray(), gIri);
			if (!b)
				log.error("ERROR: unable to save " + b);
			else
				log.info(">>>>Saved to Virtuoso");
			
			log.info("Upload Duration: " + ((System.currentTimeMillis()-endConv)/1000) + " seconds");
//			boolean validRdf = WellFormedValidator.validate(rdfOutputPath);
//			if (!validRdf) {
//				log.info("INVALID RDF: "+ rdfOutputPath);
//			} 
      
    	//}

    }

 
}
