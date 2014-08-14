

package nl.knaw.dans.clarin.cmd2rdf.batch;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import org.easybatch.core.api.EasyBatchReport;
import org.easybatch.core.impl.EasyBatchEngine;
import org.easybatch.core.impl.EasyBatchEngineBuilder;
import org.easybatch.xml.XmlRecordMapper;
import org.easybatch.xml.XmlRecordReader;

public class Launcher {
//start virtuosovirtuoso-t +foreground +configfile `find /usr/local -name virtuoso.ini`
    public static void main(String[] args) throws Exception {
    	//Thread.sleep(30000);
    	File f = null;
    	if (args != null && args.length == 1)
    		f = new File (args[0]);
    	else {
    		URL resource = Launcher.class.getResource("/Users/akmi/Desktop/cmd2rdf-jobs.xml");
    		f = Paths.get(resource.toURI()).toFile();
    	}
        // Build an easy batch engine
        EasyBatchEngine easyBatchEngine = new EasyBatchEngineBuilder()
                .registerRecordReader(new XmlRecordReader("CMD2RDF", f))
                .registerRecordMapper(new XmlRecordMapper<Jobs>(Jobs.class))
                .registerRecordProcessor(new JobProcessor())
                .build();

        // Run easy batch engine
        EasyBatchReport easyBatchReport = easyBatchEngine.call();

        // Print the batch execution report
        System.out.println(easyBatchReport);

    }

}