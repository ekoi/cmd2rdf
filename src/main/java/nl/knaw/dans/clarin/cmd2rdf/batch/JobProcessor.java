package nl.knaw.dans.clarin.cmd2rdf.batch;

/**
 * @author Eko Indarto
 *
 */

import java.beans.IntrospectionException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;

import nl.knaw.dans.clarin.cmd2rdf.exception.ActionException;
import nl.knaw.dans.clarin.cmd2rdf.mt.IAction;
import nl.knaw.dans.clarin.cmd2rdf.mt.WorkerThread;
import nl.knaw.dans.clarin.cmd2rdf.store.db.ChecksumDb;
import nl.knaw.dans.clarin.cmd2rdf.util.Misc;

import org.apache.commons.io.FileUtils;
import org.apache.directmemory.DirectMemory;
import org.apache.directmemory.cache.CacheService;
import org.easybatch.core.api.AbstractRecordProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class JobProcessor  extends AbstractRecordProcessor<Jobs> {
	private static final Logger log = LoggerFactory.getLogger(JobProcessor.class);
	private final Pattern pattern = Pattern.compile("\\{(.*?)\\}");
	private static final String URL_DB = "urlDB";
	private static final Map<String, String> GLOBAL_VARS = new HashMap<String, String>();
	private static CacheService<Object, Object> cacheService;
	

	public void processRecord(Jobs job)
			throws Exception {
		setupGlolbalConfiguration(job);
		initiateCacheService();
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
		
		fillInCacheService();
		
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
		cacheService.clear();
        try {
			cacheService.close();
		} catch (IOException e) {
			log.error("ERROR caused by IOException, msg:  " + e.getMessage());
		}	
	}
	private void fillInCacheService() {
		String profilesCacheDir = GLOBAL_VARS.get("profilesCacheDir");
		 Collection<File> profiles = FileUtils.listFiles(new File(profilesCacheDir),new String[] {"xml"}, true);
		 for (File profile:profiles) {
			 loadFromFile(profile);
		 }
	}
	private void initiateCacheService() {
		 cacheService = new DirectMemory<Object, Object>()
				    .setNumberOfBuffers( 75 )
				    .setSize( 1000000 )
				    .setInitialCapacity( 10000 )
				    .setConcurrencyLevel( 4 )
				    .newCacheService();
		 cacheService.scheduleDisposalEvery(30,TimeUnit.MINUTES);
		 
		 
	}
	
	private void loadFromFile(File file) {
		String filename = file.getName();
		log.debug("Read cache from file and put in the cache service. Filename:  " + filename + "\tFile abspath: " + file.getAbsolutePath());
		try {
			byte[] bytes = FileUtils.readFileToByteArray(file);
			cacheService.putByteArray(filename, bytes);
		} catch (IOException e) {
			log.error("FATAL ERROR: could not put the profile (filename: '" + filename + "') to the cache. Caused by IOException, msg: " + e.getMessage());
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
			if (clazzAction == null)
				log.error("FATAL ERROR: " + act.name + " is null.");
			else 
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
		Class<IAction> clazz = (Class<IAction>) Class.forName(act.clazz.name);
		Constructor[] constructors = clazz.getConstructors(); 
		for (Constructor c:constructors) {
			Class[] parameterTypes = c.getParameterTypes();
			if (parameterTypes.length == 0) {
				IAction clazzAction = clazz.newInstance();
				clazzAction.startUp(Misc.mergeVariables(JobProcessor.GLOBAL_VARS,act.clazz.property));
				return clazzAction;
			} else if (parameterTypes.length == 1 && (parameterTypes[0].isInstance(cacheService))) {
				log.debug("USING CACHE SERVICE - hashcode: " + cacheService.hashCode() + " ENTRIES: " + cacheService.entries());
				Constructor<IAction> ctor = clazz.getDeclaredConstructor(CacheService.class);
			    ctor.setAccessible(true);
			    IAction clazzAction = ctor.newInstance(cacheService);
				clazzAction.startUp(Misc.mergeVariables(JobProcessor.GLOBAL_VARS,act.clazz.property));
				return clazzAction;
				
			}
		}
		return null;
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