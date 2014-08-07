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
    private String path;
	private VirtuosoStore virtuosoStore;
	private List<Converter> converters;
	

    public WorkerThread(String path, List<Converter> converters
    					, VirtuosoStore virtuosoStore){
        this.path = path;
        this.converters = converters;
        this.virtuosoStore = virtuosoStore;
        
    }
    
//	public WorkerThread(String path, List<Converter> converters,
//			VirtuosoStore virtuosoStore, boolean saveRdfFileToHd) {
//		this.path = path;
//		this.converters = converters;
//		this.virtuosoStore = virtuosoStore;
//
//	}

    public void run() {
    	
    	log.debug("=== run ===");
        try {
			processTransformation(path);
		} catch (ConverterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    private void processTransformation(String path) throws ConverterException {
			File file = new File(path);
			if (file.exists()) {
				String rdfFilename =  file.getAbsolutePath().replace(".xml", ".rdf");
				log.debug(file.getName() + " has size of " + file.length() + " bytes (" + (file.length()/1024) + " MB).");
				
				Object object = file;
				
				//Do conversion
				for(Converter converter : converters) {
					long start = System.currentTimeMillis();
					object = converter.transform(object);
					long endConv = System.currentTimeMillis();
					log.info("Duration of Conversion: " + ((endConv-start)) + " milliseconds");
				}
				ByteArrayOutputStream bos = (ByteArrayOutputStream) object;
				
				//Upload to virtuoso
				uploadRdfToVirtuosoServer(rdfFilename, bos);
			
			
//				boolean validRdf = WellFormedValidator.validate(rdfOutputPath);
//				if (!validRdf) {
//					log.info("INVALID RDF: "+ rdfOutputPath);
//				} 
			} else {
				log.error("ERROR: '" + path + "' does not exist.");
			}

    }

	private void uploadRdfToVirtuosoServer(String rdfFilename,
			ByteArrayOutputStream bos) {
		if ( bos.size() > 0) {
			String gIri = rdfFilename.replace(virtuosoStore.getReplacedPrefixBaseURI(), virtuosoStore.getPrefixBaseURI());
			gIri = gIri.replace(".xml", ".rdf");
			log.debug("===Upload to Virtuoso, Graph IRI: " + gIri + ". Size: " + bos.size());
			
			boolean b = virtuosoStore.save(bos.toByteArray(), gIri);
			if (!b)
				log.error("ERROR: unable to save " + b);
			else
				log.info(">>>>Saved to Virtuoso");
		} else {
			log.error("========= FATAL ERROR ========= bos size is null");
		}
	}

	private void saveRdf(String filename, ByteArrayOutputStream bos) {

		log.debug("SAVE " + filename);

		try {
			FileOutputStream fosOe = new FileOutputStream(new File(filename));
			bos.writeTo(fosOe);
			fosOe.close();
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

 
}
