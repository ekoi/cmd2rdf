/**
 * 
 */
package nl.knaw.dans.clarin;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

/**
 * @author akmi
 *
 java -jar Cmd2rdf.jar -i /data/cmdi2rdf/resultsets/results/cmdi -x /data/cmdi2rdf/xsl/CMDRecord2RDF.xsl -o /data/cmdi2rdf/eko-rdf-result -c /data/cmdi2rdf/eko-cache -b http://localhost:8000/DAV -n 2
 */
public class ConverterApps {
	private static int count;
	private static final Logger log = LoggerFactory.getLogger(ConverterApps.class);
    public static void main(String[] args) {  
    	boolean ok = true;
    	JSAPResult config = null;
    	try {
			config = checkArgument(args);
			ok = config.success();
		} catch (JSAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (!ok)
    		System.exit(1);
    	
    	
    	DateTime start = new DateTime();
    	log.trace("args" + args.toString());
//    	args = new String[]{
//    			"/Users/akmi/Dropbox/DANS/IN_PROGRESS/CMDI2RDF-Workspace/cmd-xml"//0
//    			, "/Users/akmi/git/cmd2rdf/src/main/resources/xsl/CMDRecord2RDF.xsl"//1
//    			, "/Users/akmi/eko99"//2
//    			, "http://localhost:8081/DAV"//3
//    			, "/Users/akmi/eko-cmd2rdf-cache"};//4
//    	String xmlSourcePathDir = args[0];
//    	String xsltPath = args[1];
//    	String rdfOutpuDir = args[2];
//    	String baseURI = args[3];
//    	String cacheBasePathDir = args[4];
    	
    	String xmlSourcePathDir = config.getString("xmlSourcePathDir");
    	String xsltPath = config.getString("xsltPath");
    	String rdfOutpuDir = config.getString("rdfOutpuDir");
    	String baseURI = config.getString("baseURI");
    	String cacheBasePathDir = config.getString("cacheBasePathDir");
    	int maxNumberOfFile = config.getInt("maxNumberOfFile");
    	
    	String OS = System.getProperty("os.name").toLowerCase();
    	if (cacheBasePathDir == null) {
    		cacheBasePathDir = "/tmp/cmd2rdf-cache";
    		if (OS.indexOf("win") > 1 )
    			cacheBasePathDir = "C:/temp/cmd2rdf-cache";
    	}
    	
    	boolean convertAll = true;
    	if (maxNumberOfFile > 0)
    		convertAll = false;
    	
    	Converter c = new Converter(xsltPath, cacheBasePathDir);
    	
    	Iterator<File> iter = FileUtils.iterateFiles(new File(xmlSourcePathDir),new String[] {"xml"}, true);
    	while (iter.hasNext() && (convertAll || count < maxNumberOfFile) ) {
    		File f = iter.next();
    		String relativeFilePath =  f.getAbsolutePath().replace(xmlSourcePathDir, "").replace(".xml", ".rdf");
    		String base = baseURI + relativeFilePath;
    		log.debug("Converting ... [n= " + count + "]" );
    		c.simpleTransform(f.getAbsolutePath(), rdfOutpuDir + relativeFilePath, base);
    		count++;
    		
    	}
    	
    	DateTime end = new DateTime();
    	Period duration = new Period(start, end);
    	log.info("Number of xml files: " + count);
    	log.info("duration in Hours: " + duration.getHours());
    	log.info("duration in Minutes: " + duration.getMinutes());
    	log.info("duration in Seconds: " + duration.getSeconds());
    	
    	
    }  
    
    private static JSAPResult checkArgument(String[] args) throws JSAPException {
    	JSAP jsap = new JSAP();
     
        FlaggedOption opt1 = new FlaggedOption("xmlSourcePathDir")
                                .setStringParser(JSAP.STRING_PARSER)
                                .setRequired(true) 
                                .setShortFlag('i') 
                                .setLongFlag("inputXml");

        opt1.setHelp("The xmlSourcePathDir.");
        jsap.registerParameter(opt1);
        
        FlaggedOption opt2 = new FlaggedOption("xsltPath")
						        .setStringParser(JSAP.STRING_PARSER)
						        .setRequired(true) 
						        .setShortFlag('x') 
						        .setLongFlag("xsl"); 

		opt2.setHelp("The xsltPath.");
		jsap.registerParameter(opt2);
		
        FlaggedOption opt3 = new FlaggedOption("rdfOutpuDir")
						        .setStringParser(JSAP.STRING_PARSER)
						        .setRequired(true) 
						        .setShortFlag('o') 
						        .setLongFlag("rdfOutput"); 
		
		opt3.setHelp("The rdfOutpuDir.");
		jsap.registerParameter(opt3);
		
		FlaggedOption opt4 = new FlaggedOption("baseURI")
						        .setStringParser(JSAP.STRING_PARSER)
						        .setRequired(true) 
						        .setShortFlag('b') 
						        .setLongFlag("baseURI"); 

		opt4.setHelp("The baseURI.");
		jsap.registerParameter(opt4);
		
		FlaggedOption opt5 = new FlaggedOption("cacheBasePathDir")
								.setStringParser(JSAP.STRING_PARSER)
								.setRequired(false) 
								.setShortFlag('c') 
								.setLongFlag("cache"); 
		
		opt5.setHelp("(Optional)The cacheBasePathDir. When this argument is not specified, "
				+ "the '/tmp/cmd2rdf-cache' of in windows will be 'C:/tmp/cmd2rdf-cache' will be used.");
		jsap.registerParameter(opt5);
		
		FlaggedOption opt6 = new FlaggedOption("maxNumberOfFile")
								.setStringParser(JSAP.INTEGER_PARSER)
								.setDefault("0") 
								.setRequired(false) 
								.setShortFlag('n') 
								.setLongFlag("numfile"); 

		opt6.setHelp("(Optional)The number of maximum file to be converted.");
		jsap.registerParameter(opt6);
        
        Switch sw1 = new Switch("verbose")
                        .setShortFlag('v')
                        .setLongFlag("verbose");
        
        sw1.setHelp("Requests verbose output.");
        jsap.registerParameter(sw1);
        
        UnflaggedOption opt20 = new UnflaggedOption("name")
                                .setStringParser(JSAP.STRING_PARSER)
                                .setDefault("World")
                                .setRequired(true)
                                .setGreedy(true);
        
        opt20.setHelp("?");
        //jsap.registerParameter(opt20);
        
        JSAPResult config = jsap.parse(args);    

        if (!config.success()) {
            
            System.err.println();

            // print out specific error messages describing the problems
            // with the command line, THEN print usage, THEN print full
            // help.  This is called "beating the user with a clue stick."
            for (Iterator errs = config.getErrorMessageIterator();
                    errs.hasNext();) {
                System.err.println("Error: " + errs.next());
            }
            
            System.err.println();
            System.err.println("Usage: java -jar Cmd2rdf");
            System.err.println("                "
                                + jsap.getUsage());
            System.err.println();
            System.err.println(jsap.getHelp());
            System.exit(1);
        }
//        
//        String[] names = config.getStringArray("name");
//        for (int i = 0; i < config.getInt("count"); ++i) {
//            for (int j = 0; j < names.length; ++j) {
//                System.out.println((config.getBoolean("verbose") ? "Hello" : "Hi")
//                                + ", "
//                                + names[j]
//                                + "!");
//            }
//        }
        return config;
    }

}
