package nl.knaw.dans.clarin.cmd2rdf.store.db;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChecksumDbMain {
	private static final Logger log = LoggerFactory.getLogger(ChecksumDbMain.class);
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
			db.closeDbConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	public void updateStatus() {
//		ChecksumDb db = new ChecksumDb(urlDB);
//		try {
//			int nrecs = db.getTotalNumberOfRecords();
//			log.debug("TOTAL: " + nrecs);
//			nrecs = db.getTotalNumberOfDoneRecords();
//			log.debug("Number of records with DONE status: " + nrecs);
//			db.updateStatus(Enum.valueOf(ActionStatus.class, actionStatus));
//			nrecs = db.getTotalNumberOfDoneRecords();
//			log.debug("Number of records with DONE status: " + nrecs);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		db.shutdown();
//	}
//	
	public void cleanup(){}
	
	public static void main(String[] args) {
		String dbname = ChecksumDb.DB_NAME;
		String basefolder = null;
		long l=System.currentTimeMillis();
		log.debug("START  - generateApacheMD5Checksum(file)");
		String os=System.getProperty("os.name").toLowerCase();;
		if (os.indexOf("mac") >= 0)
			basefolder = "/Users/akmi/Dropbox/DANS/IN_PROGRESS/CMDI2RDF-Workspace/data/cmd-xml";
		else 
			basefolder = "/data/cmdi2rdf/resultsets/results/cmdi";

		ChecksumDbMain cdbm = new ChecksumDbMain();
		cdbm.urlDB = "/Users/akmi/eko-db-test/" + dbname;
		cdbm.xmlSourceDir = basefolder;
		cdbm.checkDiff();
		

	}
	/*
	public static void main(String[] args) {
		String dbname = ChecksumDb.DB_NAME;
		String basefolder = null;
		long l=System.currentTimeMillis();
		log.debug("START  - generateApacheMD5Checksum(file)");
		String os=System.getProperty("os.name").toLowerCase();;
		if (os.indexOf("mac") >= 0)
			basefolder = "/Users/akmi/Dropbox/DANS/IN_PROGRESS/CMDI2RDF-Workspace/data/cmd-xml";
		else 
			basefolder = "/data/cmdi2rdf/resultsets/results/cmdi";

		Collection<File> files = FileUtils.listFiles(new File(basefolder),new String[] {"xml"}, true);
		log.debug("Number of files: " + files.size());
		log.debug("Listing process duration: " + (System.currentTimeMillis() -l)/1000 + " seconds.");
		basefolder = basefolder + "/";
		try {
			//checksumsfiles = CheckSum.digest(basefolder, listFiles);
			ChecksumDb db = new ChecksumDb(dbname);
			
			//checksumsfiles = db.dump();
			//Set<String> set = checksumsfiles.keySet();
			
			db.process(basefolder, files);
				//db.checkAndstore(basefolder, listFiles);
				int total = db.getTotalNumberOfRecords();
				
				log.debug("TOTAL RECORDS: " + total);
			
			
			log.debug("TOTAL Query DURATION: " + ChecksumDb.getTotalQueryDuration() + " milliseconds.");
			log.debug("TOTAL MD5 HASHING DURATION: " + ChecksumDb.getTotalMD5GeneratedTime() + " milliseconds.");
			log.debug("TOTAL DB PROCESSING DURATION: " + ChecksumDb.getTotalDbProcessingTime() + " milliseconds.");
			log.debug("NEW: " + db.getTotalNumberOfNewRecords());
			log.debug("DONE: " + db.getTotalNumberOfDoneRecords());
			log.debug("UPDATE: " + db.getTotalNumberOfUpdatedRecords());
			log.debug("NONE: " + db.getTotalNumberOfNoneRecords());
			
			//db.printAll();
			db.shutdown();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.debug("======= DURATION TIME: " + ((System.currentTimeMillis() - l)/1000) + " seconds.");
		log.debug("end");
		long diff = (System.currentTimeMillis() - l);
		long diffSeconds = diff / 1000 % 60;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (60 * 60 * 1000) % 24;
		System.out.print(diffHours + " hours, ");
		System.out.print(diffMinutes + " minutes, ");
		System.out.print(diffSeconds + " seconds.");
		

	}*/

}
