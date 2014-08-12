/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.knaw.dans.clarin.cmd2rdf.batch.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akmi
 *
 */
public class Misc {
	private static final Logger log = LoggerFactory.getLogger(Misc.class);
	private final static Pattern pattern = Pattern.compile("\\{(.*?)\\}");
	public static List<Map.Entry> shortedBySize(Collection<File> listFiles) {
		log.debug("===== Processing " + listFiles.size() + " xml files.======");
    	Map<String, Long> map = new HashMap<String, Long>();
    	log.debug("===== Put in maps.======");
    	for (File f:listFiles) {
    		map.put(f.getAbsolutePath(), f.length());
    				
    	}
    	log.debug("===== Sorted by values.======");
    	List<Map.Entry> shortedMap = new ArrayList<Map.Entry>(map.entrySet());
    	Collections.sort(shortedMap,
    	         new Comparator() {
    	             public int compare(Object o1, Object o2) {
    	                 Map.Entry e1 = (Map.Entry) o1;
    	                 Map.Entry e2 = (Map.Entry) o2;
    	                 return ((Comparable) e2.getValue()).compareTo(e1.getValue());
    	             }
    	         });
    	log.debug("===== Size of shorted map: " + shortedMap.size());
		return shortedMap;
	}
	
	public static List safe( List other ) {
	    return other == null ? Collections.EMPTY_LIST : other;
	}
	
	public static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
	    return iterable == null ? Collections.<T>emptyList() : iterable;
	}
	
	public static String subtituteGlobalValue(Map<String, String> globalVars, String pVal) {
		Matcher m = pattern.matcher(pVal);
		if (m.find()) {
			String globalVar = m.group(1);
			if (globalVars.containsKey(globalVar)) {
				pVal = pVal.replace(m.group(0),
						globalVars.get(globalVar));
				System.out.println("pVal contains global, pVal: "
						+ pVal);
			}
		}
		return pVal;
	}

	public static Map<String, String> mergeVariables(
			Map<String, String> globalVars, List<Property> localVars) {
		Map<String,String> vars = new HashMap<String,String>(globalVars);
		for (Property arg : Misc.emptyIfNull(localVars)) {
			String pName = arg.name;
			String pVal = arg.value;
			pVal = Misc.subtituteGlobalValue(globalVars, pVal);
			vars.put(pName, pVal);
		}
		return vars;
	}

}
