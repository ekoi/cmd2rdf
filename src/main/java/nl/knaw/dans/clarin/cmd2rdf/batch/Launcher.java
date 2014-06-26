

package nl.knaw.dans.clarin.cmd2rdf.batch;

import org.easybatch.core.api.EasyBatchReport;
import org.easybatch.core.impl.EasyBatchEngine;
import org.easybatch.core.impl.EasyBatchEngineBuilder;
import org.easybatch.xml.XmlRecordMapper;
import org.easybatch.xml.XmlRecordReader;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

public class Launcher {

    public static void main(String[] args) throws Exception {
    	URL resource = Launcher.class.getResource("/cmd2rdf-jobs.xml");
    	File f = Paths.get(resource.toURI()).toFile();
    	System.out.println(f);
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