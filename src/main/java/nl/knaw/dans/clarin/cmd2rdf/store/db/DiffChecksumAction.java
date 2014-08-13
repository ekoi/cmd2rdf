package nl.knaw.dans.clarin.cmd2rdf.store.db;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import nl.knaw.dans.clarin.cmd2rdf.exception.ActionException;
import nl.knaw.dans.clarin.cmd2rdf.mt.IAction;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffChecksumAction implements IAction {
	private static final Logger log = LoggerFactory.getLogger(DiffChecksumAction.class);
	private String urlDB;
	private String xmlSourceDir;
	private String actionStatus;
	public void checkDiff() {
		log.debug("ChecksumDbMain variables: ");
		log.debug("dbname: " + urlDB);
		log.debug("xmlSrcPathDir: " + xmlSourceDir);
		long l=System.currentTimeMillis();
		log.debug("START  - generateFastMD5Checksum(file)");

		Collection<File> files = FileUtils.listFiles(new File(xmlSourceDir),new String[] {"xml"}, true);
		log.debug("Number of files: " + files.size());
		log.debug("Listing process duration: " + (System.currentTimeMillis() -l)/1000 + " seconds.");
		try {
			ChecksumDb db = new ChecksumDb(urlDB);
			
			db.process(xmlSourceDir, files);
				//db.checkAndstore(basefolder, listFiles);
			int total = db.getTotalNumberOfRecords();
				
			log.debug("TOTAL: " + total);
			
			
			log.debug("TOTAL Query DURATION: " + ChecksumDb.getTotalQueryDuration() + " milliseconds");
			log.debug("TOTAL MD5 HASHING DURATION: " + ChecksumDb.getTotalMD5GeneratedTime() + " milliseconds");
			log.debug("TOTAL DB PROCESSING DURATION: " + ChecksumDb.getTotalDbProcessingTime() + " milliseconds");
			log.debug("NEW: " + db.getTotalNumberOfNewRecords());
			log.debug("DONE: " + db.getTotalNumberOfDoneRecords());
			log.debug("UPDATE: " + db.getTotalNumberOfUpdatedRecords());
			log.debug("NONE: " + db.getTotalNumberOfNoneRecords());
			
			//db.printAll();
			db.shutdown();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void startUp(Map<String, String> vars) throws ActionException {
		urlDB = vars.get("urlDB");
		xmlSourceDir = vars.get("xmlSourceDir");
		//actionStatus = vars.get("actionStatus");
		
		if (urlDB == null || urlDB.isEmpty())
			throw new ActionException("urlDB is null or empty");
		if (xmlSourceDir == null || xmlSourceDir.isEmpty())
			throw new ActionException("xmlSourceDir is null or empty");
//		if (actionStatus == null || actionStatus.isEmpty())
//			throw new ActionException("actionStatus is null or empty");
		

	}

	public Object execute(String path, Object object) throws ActionException {
		long l=System.currentTimeMillis();
		Collection<File> files = FileUtils.listFiles(new File(xmlSourceDir),new String[] {"xml"}, true);
		log.debug("Number of files: " + files.size());
		log.debug("Listing process duration: " + (System.currentTimeMillis() -l)/1000 + " seconds.");
		try {
			ChecksumDb db = new ChecksumDb(urlDB);
			
			db.process(xmlSourceDir, files);
			int total = db.getTotalNumberOfRecords();
				
			log.debug("TOTAL: " + total);
			
			
			log.debug("TOTAL Query DURATION: " + ChecksumDb.getTotalQueryDuration() + " milliseconds");
			log.debug("TOTAL MD5 HASHING DURATION: " + ChecksumDb.getTotalMD5GeneratedTime() + " milliseconds");
			log.debug("TOTAL DB PROCESSING DURATION: " + ChecksumDb.getTotalDbProcessingTime() + " milliseconds");
			log.debug("NEW: " + db.getTotalNumberOfNewRecords());
			log.debug("DONE: " + db.getTotalNumberOfDoneRecords());
			log.debug("UPDATE: " + db.getTotalNumberOfUpdatedRecords());
			log.debug("NONE: " + db.getTotalNumberOfNoneRecords());
			
			db.shutdown();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void shutDown() throws ActionException {
		// TODO Auto-generated method stub
		
	}

}
