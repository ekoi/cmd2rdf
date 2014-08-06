/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.knaw.dans.clarin.cmd2rdf.store.VirtuosoStore;
import nl.knaw.dans.clarin.cmd2rdf.util.Misc;

import org.apache.commons.io.FileUtils;
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
	
	private String xmlSrcPathDir;
	private String urlDB;
	private String dbQueryCondition;
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
	
	public static void main(String args[]) {
		MTConverter mtc = new MTConverter();
		mtc.setBaseURI("http://localhost:8888/DAV");
		mtc.setCacheBasePathDir("/Users/akmi/eko-cache-profiles");
		mtc.setnThreads("2");
		mtc.setRdfOutputDir("/Users/akmi/eko-rdf-output");
		mtc.setRegistry("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/");
		mtc.setXsltPath("/Users/akmi/Dropbox/DANS/IN_PROGRESS/CMDI2RDF-Workspace/CMD2RDF-SVN/CMD2RDF/trunk/xsl/CMDRecord2RDF.xsl");
		mtc.setRegistry("registry");
		
		//Go to db
		mtc.setXmlSrcPathDir("/Users/akmi/eko-xml-data");
		
		
		mtc.process();
	}
	
	public void process() {  
		log.debug("MTConverter variables: ");
		log.debug("virtuosoUrl: " + virtuosoUrl);
		log.debug("virtuosoUser: " + virtuosoUser);
		log.debug("virtuosoPass: " + virtuosoPass);
		log.debug("xmlSrcPathDir: " + xmlSrcPathDir);
		log.debug("xsltPath: " + xsltPath);
		log.debug("cacheBasePathDir: " + cacheBasePathDir);
		log.debug("registry: " + registry);
		log.debug("baseURI: " + baseURI);
		log.debug("nThreads: " + nThreads);
		
		//VirtuosoStore virtuosoStore = new VirtuosoStore("http://localhost:8000/sparql-graph-crud-auth", "dba", "dba");
		VirtuosoStore virtuosoStore = new VirtuosoStore(virtuosoUrl, virtuosoUser, virtuosoPass);
		System.out.println(cacheBasePathDir);
    	log.debug("===== Collect list of files.======");
    	
    	XmlToRdfConverter converter = new XmlToRdfConverter(xmlSrcPathDir, xsltPath, cacheBasePathDir, registry, baseURI);
    	converter.startUpCacheService();
    	
    	Collection<File> listFiles = FileUtils.listFiles(new File(xmlSrcPathDir),new String[] {"xml"}, true);
    	List<Map.Entry> shortedMap = Misc.shortedBySize(listFiles);
    	
    	List<File> lf = new ArrayList<File>();
    	for (Map.Entry e : shortedMap) {
    	       lf.add(new File((String) e.getKey()));
    	}
    	
    	log.debug("===== Multithreading Processing " + lf.size() + " list of files.======");
    	
    	
    	execute(Integer.parseInt(nThreads), lf, converter, virtuosoStore);
    	converter.shutDownCacheService();
    
    }

	
	private void execute(int nThreads, List<File> files, XmlToRdfConverter converter,
			 VirtuosoStore virtuosoStore) {
	    System.out.println(nThreads);
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		log.info("@@@ begin of execution, size: " + files.size() );
    	 for (File file : files) {
    		 Runnable worker = new WorkerThread(file, converter, xmlSrcPathDir, baseURI, virtuosoStore);
             executor.execute(worker);
           }
         executor.shutdown();
         
         while (!executor.isTerminated()) {}
         
         log.info("Finished all threads");
	}

	public String getXmlSrcPathDir() {
		return xmlSrcPathDir;
	}

	public void setXmlSrcPathDir(String xmlSrcPathDir) {
		this.xmlSrcPathDir = xmlSrcPathDir;
	}

	public String getUrlDB() {
		return urlDB;
	}

	public void setUrlDB(String urlDB) {
		this.urlDB = urlDB;
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
