package nl.knaw.dans.clarin.cmd2rdf.batch;

/**
 * @author Eko Indarto
 *
 */

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.knaw.dans.clarin.cmd2rdf.exception.ActionException;
import nl.knaw.dans.clarin.cmd2rdf.mt.IAction;
import nl.knaw.dans.clarin.cmd2rdf.mt.WorkerThread;
import nl.knaw.dans.clarin.cmd2rdf.store.db.ChecksumDb;
import nl.knaw.dans.clarin.cmd2rdf.util.Misc;

import org.easybatch.core.api.AbstractRecordProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class JobProcessor  extends AbstractRecordProcessor<Jobs> {
	private static final Logger log = LoggerFactory.getLogger(JobProcessor.class);
	private final Pattern pattern = Pattern.compile("\\{(.*?)\\}");
	private static final String URL_DB = "urlDB";
	private static final Map<String, String> GLOBAL_VARS = new HashMap<String, String>();
	

	public void processRecord(Jobs job)
			throws Exception {
		setupGlolbalConfiguration(job);
		doPrepare(job.getPrepare().actions);
		doProcessRecord(job.records);
		doCleanup(job.getCleanup().actions);
	}
	private void setupGlolbalConfiguration(Jobs job)
			throws IntrospectionException, 
					IllegalAccessException,
					InvocationTargetException {
		log.debug("Setup the global configuration");
		Config c = job.getConfig();
		List<Property> props = c.property;
		for (Property prop:props) {
			GLOBAL_VARS.put(prop.name, prop.value);
		}
		//iterate through map, find whether map values contain {val}
		for (Map.Entry<String, String> e : GLOBAL_VARS.entrySet()) {
			String pVal = e.getValue();
			Matcher m = pattern.matcher(pVal);
			if (m.find()) {
				String globalVar = m.group(1);
				if (GLOBAL_VARS.containsKey(globalVar)) {
					pVal = pVal.replace(m.group(0), GLOBAL_VARS.get(globalVar));
					GLOBAL_VARS.put(e.getKey(), pVal);
				}
			}
		}
	}
	
	private void doPrepare(List<Action> list) 
			throws ClassNotFoundException,
					InstantiationException, IllegalAccessException,
					NoSuchFieldException, NoSuchMethodException,
					InvocationTargetException, ActionException {
		log.debug("Execute prepare actions.");
		List<IAction> actions = new ArrayList<IAction>();
		for (Action act : list) {
			IAction clazzAction = startUpAction(act);				
			actions.add(clazzAction);
		}
		for(IAction action : actions) {
			action.execute(null,null);
		}
		for(IAction action : actions) {
			action.shutDown();
		}
	}
	
	private void doProcessRecord(List<Record> records)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NoSuchFieldException,
			NoSuchMethodException, InvocationTargetException, ActionException {
		log.debug("Execute records.");	
		for(Record r:records) {
			List<String> paths = null;
			if (r.xmlSource.contains(URL_DB)) {
				String urlDB = subtituteGlobalValue(r.xmlSource);
				ChecksumDb cdb = new ChecksumDb(urlDB);
		    	paths = cdb.getRecords(Misc.convertToActionStatus(r.filter));
		    	cdb.closeDbConnection();
			}
			
			List<IAction> actions = new ArrayList<IAction>();
			List<Action> list = r.actions;
			for (Action act : list) {
				IAction clazzAction = startUpAction(act);				
				actions.add(clazzAction);
			}
			if (r.nThreads>1) 
				doMultithreadingAction(r, paths, actions);
			else {
				for(IAction action : actions) {
					for (String path:paths)
						action.execute(path,null);
				}
			}
	         
			for(IAction action : actions) {
				action.shutDown();
			}
		}
			
	}
	private void doMultithreadingAction(Record r, List<String> paths,
			List<IAction> actions) {
		log.debug("Multithreading is on, number of threads: " + r.nThreads);
		ExecutorService executor = Executors.newFixedThreadPool(r.nThreads);
		log.info("Number of processed records files: " + paths.size() );
		for (String path : paths) {
			 Runnable worker = new WorkerThread(path, actions);
		     executor.execute(worker);
		}
		executor.shutdown();
		 
		while (!executor.isTerminated()) {}
		log.info("===Finished all threads===");
	}
	
	private void doCleanup(List<Action> list) 
				throws ClassNotFoundException, InstantiationException, 
					IllegalAccessException, NoSuchFieldException, 
					NoSuchMethodException, InvocationTargetException, ActionException {
		log.debug("Execute cleanup part.");	
		List<IAction> actions = new ArrayList<IAction>();
		for (Action act : list) {
			IAction clazzAction = startUpAction(act);				
			actions.add(clazzAction);
		}
		for(IAction action : actions) {
			action.execute(null,null);
		}
		for(IAction action : actions) {
			action.shutDown();
		}
	}

	
	private IAction startUpAction(Action act)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NoSuchFieldException,
			NoSuchMethodException, InvocationTargetException, ActionException {
		@SuppressWarnings("unchecked")
		Class<IAction> clazz = (Class<IAction>) Class.forName(act.clazz.name);
		IAction clazzAction = clazz.newInstance();
		clazzAction.startUp(Misc.mergeVariables(JobProcessor.GLOBAL_VARS,act.clazz.property));
		return clazzAction;
	}

	private String subtituteGlobalValue(String pVal) {
		Matcher m = pattern.matcher(pVal);
		if (m.find()) {
			String globalVar = m.group(1);
			if (GLOBAL_VARS.containsKey(globalVar)) {
				pVal = pVal.replace(m.group(0),
						GLOBAL_VARS.get(globalVar));
			}
		}
		return pVal;
	}
}
