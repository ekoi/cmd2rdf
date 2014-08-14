package nl.knaw.dans.clarin.cmd2rdf.store.db;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import nl.knaw.dans.clarin.cmd2rdf.exception.ActionException;
import nl.knaw.dans.clarin.cmd2rdf.mt.IAction;
import nl.knaw.dans.clarin.cmd2rdf.util.ActionStatus;
import nl.knaw.dans.clarin.cmd2rdf.util.Misc;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MD5ChecksumDBAction implements IAction {
	private static final Logger log = LoggerFactory.getLogger(MD5ChecksumDBAction.class);
	private ChecksumDb db;
	private String urlDB;
	private String xmlSourceDir;
	private String action;
	private String status;
	
	public void startUp(Map<String, String> vars) throws ActionException {
		urlDB = vars.get("urlDB");
		xmlSourceDir = vars.get("xmlSourceDir");
		action = vars.get("action");
		status = vars.get("status");
		
		if (urlDB == null || urlDB.isEmpty())
			throw new ActionException("urlDB is null or empty");
		if (action == null || action.isEmpty())
			throw new ActionException("action is null or empty");
		db = new ChecksumDb(urlDB);

	}

	public Object execute(String path, Object object) throws ActionException {
		ActionStatus act = Misc.convertToActionStatus(action);
		ActionStatus actStatus = Misc.convertToActionStatus(status);
		switch(act){
			case CHECKSUM_DIFF: checksumDiff();
				break;
			case UPDATE: db.updateDoneStatusToDelete(actStatus);
				break;
			default:
				return null;
		}		
		
		return null;
	}

	private void checksumDiff() throws ActionException {
		if (xmlSourceDir == null || xmlSourceDir.isEmpty())
			throw new ActionException("xmlSourceDir is null or empty");
		Collection<File> files = FileUtils.listFiles(new File(xmlSourceDir),new String[] {"xml"}, true);
		log.debug("Number of files: " + files.size());
		try {
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
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutDown() throws ActionException {
		db.shutdown();
	}

}
