package nl.knaw.dans.clarin.cmd2rdf.batch;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.knaw.dans.clarin.cmd2rdf.util.Misc;

import org.easybatch.core.api.AbstractRecordProcessor;
public class JobProcessor  extends AbstractRecordProcessor<Jobs> {
	private final Pattern pattern = Pattern.compile("\\$(.*?)\\$");
	private static final Map<String, String> GLOBAL_VARS = new HashMap<String, String>();
	

	public void processRecord(Jobs job)
			throws Exception {
		setupGlolbalConfiguration(job);
		Prepare p = job.getPrepare();
		//doProcess(job.getPrepare().actions);
		List<Record> records = job.records;
		doProcess(records.get(0).actions);
	}


	private void doProcess(List<Action> list) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			NoSuchFieldException, NoSuchMethodException,
			InvocationTargetException {
		for (Action act : list) {
			System.out.println(act.clazz.name);
			Class<?> clazz = Class.forName(act.clazz.name);
			Object clazzObj = clazz.newInstance();
			List<Arg> args = act.clazz.arguments;
			for (Arg arg : Misc.emptyIfNull(args)) {
				String pName = arg.name;
				System.out.println(pName);
				String pVal = arg.value;
				Matcher m = pattern.matcher(pVal);
				if (m.find()) {
					String globalVar = m.group(1);
					if (GLOBAL_VARS.containsKey(globalVar)) {
						pVal = pVal.replace(m.group(0), GLOBAL_VARS.get(globalVar));
					}
				}
				Field f = clazz.getDeclaredField(pName);
				System.out.println(f.getName());
				f.setAccessible(true);
				f.set(clazzObj, pVal);
				System.out.println(f.get(clazzObj));
				
		    }
			System.out.println(act.clazz.methodToExecute);
			if (act.clazz.methodToExecute != null) {
				Method method = clazz.getDeclaredMethod(act.clazz.methodToExecute);
				method.invoke(clazzObj);
			}
		}
	}


	private void setupGlolbalConfiguration(Jobs job)
			throws IntrospectionException, IllegalAccessException,
			InvocationTargetException {
		Config c = job.getConfig();
		BeanInfo beanInfo = Introspector.getBeanInfo(Config.class);
		for (PropertyDescriptor propertyDesc : beanInfo.getPropertyDescriptors()) {
		    String propertyName = propertyDesc.getName();
		    Object value = propertyDesc.getReadMethod().invoke(c);
		    GLOBAL_VARS.put(propertyName, String.valueOf(value));
		}
	}
	
}
