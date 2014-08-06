package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import nl.knaw.dans.clarin.cmd2rdf.exception.ConverterException;
import nl.knaw.dans.clarin.cmd2rdf.store.VirtuosoStore;
import nl.knaw.dans.clarin.cmd2rdf.util.WellFormedValidator;

import org.apache.directmemory.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerThread implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(WorkerThread.class);
    private File file;
	private String xmlSrcPathDir;
	private String baseURI;
	private Converter converter;
	private VirtuosoStore virtuosoStore;
	

    public WorkerThread(File file, Converter converter, String xmlSrcPathDir, String baseURI
    					, VirtuosoStore virtuosoStore){
        this.file = file;
        this.converter = converter;
        this.xmlSrcPathDir = xmlSrcPathDir;
        this.baseURI = baseURI;
        this.virtuosoStore = virtuosoStore;
        
    }

    public void run() {
    	
    	log.debug("=== run ===");
        try {
			processTransformation(file);
		} catch (ConverterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    private void processTransformation(File file) throws ConverterException {
		//for (File file: subSets) {
			String rdfFilename =  file.getAbsolutePath().replace(".xml", ".rdf");
			log.debug(file.getName() + " has size of " + file.length() + " bytes (" + (file.length()/1024) + " MB).");
			long start = System.currentTimeMillis();
			ByteArrayOutputStream bos = converter.simpleTransform(file);
			long endConv = System.currentTimeMillis();
			log.info("Duration of Conversion: " + ((endConv-start)) + " milliseconds");
			String gIri = rdfFilename.replace(xmlSrcPathDir, baseURI);
			gIri = gIri.replace(".xml", ".rdf");
			log.debug("===Upload to Virtuoso, Graph IRI: " + gIri + ". Size: " + bos.size());
			if ( bos.size() > 0) {
//				boolean b = virtuosoStore.save(bos.toByteArray(), gIri);
//				if (!b)
//					log.error("ERROR: unable to save " + b);
//				else
//					log.info(">>>>Saved to Virtuoso");
//				} else {
//					log.error("========= FATAL ERROR ========= bos size is null");
				
			}
			try {
				String s = file.getAbsolutePath();
				s = s.replace(xmlSrcPathDir, "");
				s = s.replaceAll("/", "-");
				s = s.replace(".xml", ".rdf");
				s = "/Users/akmi/eko-rdf-output/" + s;
				log.debug("SAVE " + s);
				FileOutputStream fos = new FileOutputStream (new File(s)); 
				bos.writeTo(fos);
				fos.close();
				bos.close();
			} catch (IOException e) {
				log.error("########### ERRROR =======");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("SAVE Duration: " + ((System.currentTimeMillis()-endConv)) + " milliseconds");
//			boolean validRdf = WellFormedValidator.validate(rdfOutputPath);
//			if (!validRdf) {
//				log.info("INVALID RDF: "+ rdfOutputPath);
//			} 
      
    	//}

    }

 
}
