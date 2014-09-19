package nl.knaw.dans.clarin.cmd2rdf.batch;

/**
 * @author Eko Indarto
 *
 */

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.knaw.dans.clarin.cmd2rdf.exception.ActionException;
import nl.knaw.dans.clarin.cmd2rdf.mt.IAction;
import nl.knaw.dans.clarin.cmd2rdf.mt.WorkerCallable;
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
	private static volatile Map<String, String> GLOBAL_VARS = new HashMap<String, String>();
	private static volatile CacheService<Object, Object> cacheService;
	

	public void processRecord(Jobs job)
			throws Exception {
		setupGlolbalConfiguration(job);
		initiateCacheService();
		doPrepare(job.getPrepare().actions);
		doProcessRecord(job.records);
		doCleanup(job.getCleanup().actions);
		closeCacheService();	
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
			log.debug("###### PROCESSING OF RECORD : " + r.desc);
			List<String> paths = null;
			if (r.xmlSource.contains(URL_DB)) {
				String urlDB = subtituteGlobalValue(r.xmlSource);
				ChecksumDb cdb = new ChecksumDb(urlDB);
				if (r.xmlLimitSizeMin != null) {
					try {
						Integer.parseInt(r.xmlLimitSizeMin);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("ERROR: xmlLimitSizeMin value is not integer. " + e.getMessage());
					}
				}
					
				if (r.xmlLimitSizeMax != null) {
					try {
						Integer.parseInt(r.xmlLimitSizeMax);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("ERROR: xmlLimitSizeMax value is not integer. " + e.getMessage());
					}
				}
				
		    	paths = cdb.getRecords(Misc.convertToActionStatus(r.filter), r.xmlLimitSizeMin, r.xmlLimitSizeMax);
		    	cdb.closeDbConnection();
			}
			
			List<IAction> actions = new ArrayList<IAction>();
			List<Action> list = r.actions;
			for (Action act : list) {
				IAction clazzAction = startUpAction(act);				
				actions.add(clazzAction);
			}
			if (r.nThreads>0) 
				doCallableAction(r, paths, actions);
			else {
				for(IAction action : actions) {
					for (String path:paths)
						action.execute(path,null);
				}
			}
	         
			for(IAction action : actions) {
				action.shutDown();
			}
			if (r.cleanup != null && r.cleanup.actions != null)
				doCleanup(r.cleanup.actions);
		}
	}
	
	
	private void doCallableAction(Record r, List<String> paths,
			List<IAction> actions) {
		log.debug("Multithreading is on, number of threads: " + r.nThreads);
		int n=0;
		int i=0;
		List<List<String>> paths2 = Misc.split(paths, r.nThreads);
		int totalfiles = paths.size();
		log.debug("==============================================================");
		log.debug("Numbers of files: " + totalfiles);
		log.debug("Numbers op splitsing: " + paths2.size());
		log.debug("==============================================================");
		for (List<String> pth : paths2) {
			n++;
			ExecutorService executor = Executors.newCachedThreadPool();
			log.info(">>>>>>>>>>>>>[" + n + "]Number of processed records files: " + pth.size() );
			//List<Future<String>> futures = new ArrayList<Future<String>>();
			for (String p : pth) {
				i++;
				 Callable<String> worker = new WorkerCallable(p, actions, i);
				 executor.submit(worker);
//			     Future<String> future = executor.submit(worker);
//			     futures.add(future);
//			     try {
//					log.debug(future.get());
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (ExecutionException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
//			log.debug("============ FUTURE SIZE: " + futures.size() + "\tPATHS SIZE: " + paths.size() );
//			for (Future<String> future:futures) {
//				try {
//					log.debug("### " + future.get() );
//				} catch (InterruptedException   e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (ExecutionException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			executor.shutdown();
			while (!executor.isTerminated()) {}
			log.info("===Finished "+ pth.size() + " threads===");
			totalfiles=totalfiles-pth.size();
			log.info("REMAINS: " + totalfiles);
		}
	}
	
	private void doMultithreadingAction(Record r, List<String> paths,
			List<IAction> actions) {
		log.debug("Multithreading is on, number of threads: " + r.nThreads);
		ExecutorService executor = Executors.newFixedThreadPool(r.nThreads);
		log.info("Number of processed records files: " + paths.size() );
		int i=0;
		
		for (String path : paths) {
			i++;
			System.out.println(":::::::::::::::::::: " + i);
			 Runnable worker = new WorkerThread(path, actions);
		     Future<?> future = executor.submit(worker, "XXXXXXXXdone: " + i);
		     try {
				System.out.println(future.get());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		executor.shutdown();
		while (!executor.isTerminated()) {}
		log.info("===Finished all threads===");
	}
	
	
	private void closeCacheService() {
		log.debug("closeCacheService: CLOSE CacheService. It contains " + cacheService.entries() + " items.");
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
		 int i = 0;
		 for (File profile:profiles) {
			 i++;
			 //log.debug("i: " + i + " filename: " + profile.getName());
			 loadFromFile(profile.getName(), profile);
		 }
		 
	}
	private void initiateCacheService() {
		 cacheService = new DirectMemory<Object, Object>()
				    .setNumberOfBuffers( 100 )
				    .setSize( 15000000 )
				    .setInitialCapacity( 10000 )
				    .setConcurrencyLevel( 4 )
				    .newCacheService();
		 cacheService.scheduleDisposalEvery(30,TimeUnit.MINUTES);
		 
		 
	}
	
	private void loadFromFile(String key, File file) {
		//log.debug("Load " + key + " to Cache Service. It contains " + cacheService.entries() + " items.");
		//log.debug("Read cache from file and put in the cache service. Key:  " + key + "\tFile abspath: " + file.getAbsolutePath());
		try {
			byte[] bytes = FileUtils.readFileToByteArray(file);
			cacheService.putByteArray(key, bytes);
		} catch (IOException e) {
			log.error("FATAL ERROR: could not put the profile (key: '" + key + "') to the cache. Caused by IOException, msg: " + e.getMessage());
		}  
		//log.debug("loadFromFile: Adding new item to CacheService. Now it contains " + cacheService.entries() + " items.");
	} 
	
	
	private void doCleanup(List<Action> list) 
				throws ClassNotFoundException, InstantiationException, 
					IllegalAccessException, NoSuchFieldException, 
					NoSuchMethodException, InvocationTargetException, ActionException {
		log.debug("Execute cleanup part.");	
		List<IAction> actions = new ArrayList<IAction>();
		for (Action act : list) {
			log.debug(act.name);
			IAction clazzAction = startUpAction(act);		
			if (clazzAction == null)
				log.error("FATAL ERROR: " + act.name + " is null.");
			else 
				actions.add(clazzAction);
		}
		Object o = null;
		for(IAction action : actions) {
			
			 o =action.execute(null, o);
		}
		for(IAction action : actions) {
			action.shutDown();
		}
	}

	
	private IAction startUpAction(Action act)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NoSuchFieldException,
			NoSuchMethodException, InvocationTargetException, ActionException {
		log.debug("Startup of " + act.clazz.name);
		log.debug("Description: " + act.name);
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