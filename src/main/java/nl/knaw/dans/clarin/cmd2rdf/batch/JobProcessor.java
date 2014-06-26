package nl.knaw.dans.clarin.cmd2rdf.batch;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.knaw.dans.clarin.cmd2rdf.mt.ThreadPoolConverter;
import nl.knaw.dans.clarin.cmd2rdf.util.Misc;

import org.easybatch.core.api.AbstractRecordProcessor;
public class JobProcessor  extends AbstractRecordProcessor<Jobs> {

	

	public void processRecord(Jobs job)
			throws Exception {
		Config c = job.getConfig();
		System.out.println("config version: " + c.version);
		System.out.println("eko: " + c.virtuosoUrl);
		Record r = job.getRecord();
		List<Action> list = r.actions;
		for (Action act : list) {
			System.out.println(act.name);
			if (act.name.equals("transform")) {
				List<Argument> args = act.clazz.arguments;
				for (Argument arg : args) {
					System.out.println(">>>> " + arg.name + "\t ---" );
					List<Param> ps = arg.params;
					for (Param p : Misc.emptyIfNull(ps)) {
						System.out.println("==== " + p.name + "\t" + p.value);
						String value = p.value;
						Pattern pattern = Pattern.compile("\\$(.*?)\\$");
						Matcher m = pattern.matcher(value);
						if (m.find()) {
							String globalVar = m.group(1);
							System.out.println("globalVar: " + globalVar);
						}
					}
				}
			}
		}
		//ThreadPoolConverter tpc = new ThreadPoolConverter(xmlSourcePathDir, xsltPath, rdfOutputDir, baseURI, cacheBasePathDir, numberOfThreads)
	}
	
	

}
