package nl.knaw.dans.clarin.mt;

import java.io.File;
import java.util.List;

import nl.knaw.dans.clarin.util.WellFormedValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerThread implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(WorkerThread.class);
    private List<File> subSets;
	private String xmlSourcePathDir;
	private String baseURI;
	private String rdfOutpuDir;
	private Converter converter;
	private List<String> profilesList;

    public WorkerThread(List<File> subSets, String xmlSourcePathDir, String baseURI, String rdfOutpuDir
    					, String xsltPath, String cacheBasePathDir, List<String> profilesList){
        this.subSets = subSets;
        this.xmlSourcePathDir = xmlSourcePathDir;
        this.baseURI = baseURI;
        this.rdfOutpuDir = rdfOutpuDir;
        this.profilesList = profilesList;
        converter = new Converter(xsltPath, cacheBasePathDir);
    }

    public void run() {

    	log.debug("=== run ===");
        processTransformation();

    }
    
    private void processTransformation() {
		for (File file: subSets) {
	    	String relativeFilePath =  file.getAbsolutePath().replace(xmlSourcePathDir, "").replace(".xml", ".rdf");
			String base = baseURI + relativeFilePath;
			String rdfOutputPath = rdfOutpuDir + relativeFilePath;
			converter.simpleTransform(file.getAbsolutePath(), rdfOutputPath, base, profilesList);
			boolean validRdf = WellFormedValidator.validate(rdfOutputPath);
			if (!validRdf) {
				log.info("INVALID RDF: "+ rdfOutputPath);
			} 
      
    	}

    }

 
}
