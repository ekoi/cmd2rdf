/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.store.db;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.dans.clarin.cmd2rdf.exception.ActionException;
import nl.knaw.dans.clarin.cmd2rdf.mt.IAction;
import nl.knaw.dans.clarin.cmd2rdf.util.ActionStatus;
import nl.knaw.dans.clarin.cmd2rdf.util.Misc;

/**
 * @author Eko Indarto
 *
 */
public class MD5ChecksumRecordStatus implements IAction {
	private static final Logger log = LoggerFactory.getLogger(MD5ChecksumRecordStatus.class);
	private Map<String, String> params;
	private String urlDB;
	private String action;
	private String status;
	ChecksumDb db;

	/* (non-Javadoc)
	 * @see nl.knaw.dans.clarin.cmd2rdf.mt.IAction#startUp(java.util.Map)
	 */
	public void startUp(Map<String, String> vars) throws ActionException {
		params = vars;
		checkRequiredVariables();
		db = new ChecksumDb(urlDB);
		
	}

	/* (non-Javadoc)
	 * @see nl.knaw.dans.clarin.cmd2rdf.mt.IAction#execute(java.lang.String, java.lang.Object)
	 */
	public Object execute(String path, Object object) throws ActionException {
		log.debug(action + " status of '" + path + "' to " + status);
		ActionStatus stAs = Misc.convertToActionStatus(status);
		db.updateActionStatusByRecord(path, stAs);
		return null;
	}

	/* (non-Javadoc)
	 * @see nl.knaw.dans.clarin.cmd2rdf.mt.IAction#shutDown()
	 */
	public void shutDown() throws ActionException {
		db.closeDbConnection();

	}
	
	private void checkRequiredVariables() throws ActionException {
		urlDB = params.get("urlDB");
		action = params.get("action");
		status = params.get("status");
		if (urlDB == null || urlDB.isEmpty())
			throw new ActionException("urlDB is null or empty");
		if (action == null || action.isEmpty())
			throw new ActionException("action is null or empty");
		if (status == null || status.isEmpty())
			throw new ActionException("status is null or empty");
	}

}
