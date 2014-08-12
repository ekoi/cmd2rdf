package nl.knaw.dans.clarin.cmd2rdf.batch;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.knaw.dans.clarin.cmd2rdf.mt.WorkerThread;
import nl.knaw.dans.clarin.cmd2rdf.store.db.ChecksumDb;
import nl.knaw.dans.clarin.cmd2rdf.util.ActionStatus;
import nl.knaw.dans.clarin.cmd2rdf.util.Misc;

import org.easybatch.core.api.AbstractRecordProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class JobProcessor  extends AbstractRecordProcessor<Jobs> {
	private static final Logger log = LoggerFactory.getLogger(JobProcessor.class);
	private final Pattern pattern = Pattern.compile("\\{(.*?)\\}");
	private static final Map<String, String> GLOBAL_VARS = new HashMap<String, String>();
	

	public void processRecord(Jobs job)
			throws Exception {
		setupGlolbalConfiguration(job);
		doPrepare(job.getPrepare().actions);
		doProcessRecord(job.records.get(0));
		
		doCleanup(job.getCleanup().actions);
	}
	private void setupGlolbalConfiguration(Jobs job)
			throws IntrospectionException, 
					IllegalAccessException,
					InvocationTargetException {
		Config c = job.getConfig();
		BeanInfo beanInfo = Introspector.getBeanInfo(Config.class);
		for (PropertyDescriptor propertyDesc : beanInfo.getPropertyDescriptors()) {
		    String propertyName = propertyDesc.getName();
		    Object value = propertyDesc.getReadMethod().invoke(c);
		    System.out.println(propertyName + "\t" + value);
		    GLOBAL_VARS.put(propertyName, String.valueOf(value));
		}
		//iterate through map, find whether map values contain {val}
		for (Map.Entry<String, String> e : GLOBAL_VARS.entrySet()) {
			String pVal = e.getValue();
			Matcher m = pattern.matcher(pVal);
			if (m.find()) {
				String globalVar = m.group(1);
				if (GLOBAL_VARS.containsKey(globalVar)) {
					pVal = pVal.replace(m.group(0), GLOBAL_VARS.get(globalVar));
					System.out.println("pVal contains global, pVal: " + pVal);
					GLOBAL_VARS.put(e.getKey(), pVal);
				}
			}
		}
	}
	
	private void doPrepare(List<Action> list) 
			throws ClassNotFoundException,
					InstantiationException, IllegalAccessException,
					NoSuchFieldException, NoSuchMethodException,
					InvocationTargetException {
	for (Action act : list) {
		executeActionObject(act);
	}
}
	
	private void doProcessRecord(Record r)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NoSuchFieldException,
			NoSuchMethodException, InvocationTargetException {
			
			List<String> paths = null;
			if (r.xmlSource.contains("urlDB")) {
				String urlDB = subtituteGlobalValue(r.xmlSource);
				ChecksumDb cdb = new ChecksumDb(urlDB);
		    	paths = cdb.getRecords(Enum.valueOf(ActionStatus.class, r.filter));
		    	cdb.shutdown();
			}
			
			List<Object> objects = new ArrayList<Object>();
			List<Action> list = r.actions;
			for (Action act : list) {
				System.out.println(act.clazz.name);
				Object clazzObj = executeActionObject(act);
				
				objects.add(clazzObj);
			}
			
			ExecutorService executor = Executors.newFixedThreadPool(r.nThreads);
			log.info("@@@ begin of execution, size: " + paths.size() );
	    	for (String path : paths) {
	    		 Runnable worker = new WorkerThread(path, objects);
	             executor.execute(worker);
	        }
	         executor.shutdown();
	         
	         while (!executor.isTerminated()) {}
	         
	         log.info("Finished all threads");
			
	}
	
	private void doCleanup(List<Action> actions) 
				throws ClassNotFoundException, InstantiationException, 
					IllegalAccessException, NoSuchFieldException, 
					NoSuchMethodException, InvocationTargetException {
		for (Action act : actions) {
			executeActionObject(act);
		}
	}

	private Object executeActionObject(Action act)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, NoSuchFieldException,
			NoSuchMethodException, InvocationTargetException {
		Class<?> clazz = Class.forName(act.clazz.name);
		Object clazzObj = clazz.newInstance();
		
		List<Property> args = act.clazz.property;
		for (Property arg : Misc.emptyIfNull(args)) {
			String pName = arg.name;
			String pVal = arg.value;
			System.out.println("pName: " + pName + "\tpVal: " + pVal);
			pVal = subtituteGlobalValue(pVal);
			Field f = clazz.getDeclaredField(pName);
			f.setAccessible(true);
			f.set(clazzObj, pVal);				
		}
		
		if (act.clazz.methodToExecute != null) {
			Method method = clazz
					.getDeclaredMethod(act.clazz.methodToExecute);
			method.invoke(clazzObj);
		}
		return clazzObj;
	}

	private String subtituteGlobalValue(String pVal) {
		Matcher m = pattern.matcher(pVal);
		if (m.find()) {
			String globalVar = m.group(1);
			if (GLOBAL_VARS.containsKey(globalVar)) {
				pVal = pVal.replace(m.group(0),
						GLOBAL_VARS.get(globalVar));
				System.out.println("pVal contains global, pVal: "
						+ pVal);
			}
		}
		return pVal;
	}
}
