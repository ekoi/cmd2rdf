/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.knaw.dans.clarin.cmd2rdf.batch.Property;
import nl.knaw.dans.clarin.cmd2rdf.exception.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akmi
 *
 */
public class Misc {
	private static final Logger log = LoggerFactory.getLogger(Misc.class);
	private final static Pattern pattern = Pattern.compile("\\{(.*?)\\}");
	
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
				log.debug("pVal contains global, pVal: "
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

	public static ActionStatus convertToActionStatus(String words)
			throws ActionException {
		String[] w = words.trim().split(" ");
		int len = w.length;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			if (w[i].length() > 0) {
				sb.append(w[i]);
				if (i < len - 1)
					sb.append("_");
			}
		}
		try {
			ActionStatus s = Enum.valueOf(ActionStatus.class, sb.toString());
			return s;
		} catch (IllegalArgumentException e) {
			throw new ActionException(
					"ERROR: IllegalArgumentException, no enum constant of '"
							+ words + "'.");
		}
	}
}
