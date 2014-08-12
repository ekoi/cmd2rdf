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

import nl.knaw.dans.clarin.cmd2rdf.exception.ConverterException;
import nl.knaw.dans.clarin.cmd2rdf.store.VirtuosoClient;
import nl.knaw.dans.clarin.cmd2rdf.store.db.ChecksumDb;
import nl.knaw.dans.clarin.cmd2rdf.util.ActionStatus;
import nl.knaw.dans.clarin.cmd2rdf.util.CheckSum;
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
	private String filter;
	private String xsltPath;
	private String rdfOutputDir;
	private String baseURI;
	private String cacheBasePathDir;
	private String nThreads;
	private String registry;
	private String virtuosoUrl;
	private String virtuosoUser;
	private String virtuosoPass;
	private String xsltPathOrgEnt;
	private String vloOrgsParam;
	
	 
	public MTConverter(){
	}
	
	public static void main(String args[]) {
		MTConverter mtc = new MTConverter();
		mtc.setBaseURI("http://localhost:8888/DAV");
		mtc.setCacheBasePathDir("/Users/akmi/eko-cache-profiles");
		mtc.setnThreads("1");
		mtc.setRdfOutputDir("/Users/akmi/eko-rdf-output");
		mtc.setRegistry("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/");
		mtc.setXsltPath("/Users/akmi/Dropbox/DANS/IN_PROGRESS/CMDI2RDF-Workspace/CMD2RDF-SVN/CMD2RDF/trunk/xsl/CMDRecord2RDF.xsl");
		mtc.setRegistry("registry");
		mtc.setFilter(ActionStatus.NONE.name());
		mtc.setUrlDB("/Users/akmi/eko-db-test/DB_CMD_CHECKSUM");
		//Go to db
		mtc.setXmlSrcPathDir("/Users/akmi/eko-xml-data");
		mtc.setXsltPathOrgEnt("/Users/akmi/eko-xsl-data/addOrganisationEntity.xsl");
		mtc.setVloOrgsParam("/Users/akmi/Dropbox/DANS/IN_PROGRESS/CMDI2RDF-Workspace/tmp/meertens-VLO-orgs.rdf");
		
		mtc.setVirtuosoUrl("http://localhost:8000/sparql-graph-crud-auth?");
		mtc.setVirtuosoUser("dba");
		mtc.setVirtuosoPass("dba");
		
		
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
		
		
		
		System.out.println(cacheBasePathDir);
    	log.debug("===== Collect list of files.======");

    	ChecksumDb cdb = new ChecksumDb(urlDB);
    	List<String> paths = cdb.getRecords(Enum.valueOf(ActionStatus.class, filter));
    	cdb.shutdown();
    	log.debug("===== Multithreading Processing " + paths.size() + " list of files.======");
    	
    	
    	XsltTransformer converter = new XsltTransformer();
//    	converter.setXmlSrcPathDir(xmlSrcPathDir);
//    	converter.setXsltPath(xsltPath);
//    	converter.setCacheBasePathDir(cacheBasePathDir);
//    	converter.setRegistry(registry);
//    	converter.setBaseURI(baseURI);
    	try {
			converter.startUp();
		} catch (ConverterException e) {
			e.printStackTrace();
		}
    	OrganisationEntityConverter oeConverter = new OrganisationEntityConverter();
//    	oeConverter.setXsltPath(xsltPathOrgEnt);
//    	oeConverter.setVloOrgsParam(vloOrgsParam);
    	try {
			oeConverter.startUp();
		} catch (ConverterException e) {
			e.printStackTrace();
		}
    	
		VirtuosoClient virtuosoStore = new VirtuosoClient(virtuosoUrl, virtuosoUser, virtuosoPass);
		virtuosoStore.setReplacedPrefixBaseURI("/Users/akmi/Dropbox/DANS/IN_PROGRESS/CMDI2RDF-Workspace/data/cmd-xml");
		virtuosoStore.setPrefixBaseURI(baseURI);
    			
    	execute(Integer.parseInt(nThreads), paths, converter, oeConverter, virtuosoStore);
    	converter.shutDownCacheService();
    
    }

	
	private void execute(int nThreads, List<String> paths, XsltTransformer converter, OrganisationEntityConverter oeConverter,
			 VirtuosoClient virtuosoStore) {
		List<IAction> converters = new ArrayList<IAction>();
		converters.add(converter);
		converters.add(oeConverter);
	    System.out.println(nThreads);
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		log.info("@@@ begin of execution, size: " + paths.size() );
    	 for (String path : paths) {
    		 Runnable worker = new WorkerThread(path, converters, virtuosoStore);
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

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getXsltPathOrgEnt() {
		return xsltPathOrgEnt;
	}

	public void setXsltPathOrgEnt(String xsltPathOrgEnt) {
		this.xsltPathOrgEnt = xsltPathOrgEnt;
	}

	public String getVloOrgsParam() {
		return vloOrgsParam;
	}

	public void setVloOrgsParam(String vloOrgsParam) {
		this.vloOrgsParam = vloOrgsParam;
	}  
    
    
    

}
