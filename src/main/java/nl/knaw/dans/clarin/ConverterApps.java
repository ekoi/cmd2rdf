/**
 * 
 */
package nl.knaw.dans.clarin;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import nl.knaw.dans.clarin.util.WellFormedValidator;

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
	private static int validRdfOutput;
	private static int invalidRdfOutput;
	private static int count;
	private static final Logger log = LoggerFactory.getLogger(ConverterApps.class);
    public static void main(String[] args) {  
    	boolean ok = true;
    	JSAPResult config = null;
    	try {
			config = checkArgument(args);
			ok = config.success();
		} catch (JSAPException e) {
			log.error("ERROR: JSAPException, caused by: " + e.getCause());
		}
    	if (!ok)
    		System.exit(1);
    	
    	log.debug("### Start Conversion XML --> RDF ####");
    	DateTime start = new DateTime();
    	
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
    	
    	//Iterator<File> iter = FileUtils.iterateFiles(new File(xmlSourcePathDir),new String[] {"xml"}, true);
    	//while (iter.hasNext() && (convertAll || count < maxNumberOfFile) ) {
    	Collection<File> listFiles = FileUtils.listFiles(new File(xmlSourcePathDir),new String[] {"xml"}, true);
    	log.debug("===== Processing " + listFiles.size() + " xml files.======");
    	for (File f : listFiles) {
    		//File f = iter.next();
    		String relativeFilePath =  f.getAbsolutePath().replace(xmlSourcePathDir, "").replace(".xml", ".rdf");
    		String base = baseURI + relativeFilePath;
    		log.debug("Converting ... [n= " + count + "]" );
    		String rdfOutputPath = rdfOutpuDir + relativeFilePath;
    		c.simpleTransform(f.getAbsolutePath(), rdfOutputPath, base);
    		boolean validRdf = WellFormedValidator.validate(rdfOutputPath);
    		if (!validRdf) {
    			invalidRdfOutput++;
    			log.info("INVALID RDF: [" + invalidRdfOutput + "] path: "+ rdfOutputPath);
    		} else 
    			validRdfOutput++;
    		
    		count++;
    	}
    	
    	DateTime end = new DateTime();
    	Period duration = new Period(start, end);
    	log.info("Number of xml files: " + count);
    	log.info("Number of valid rdf: " + validRdfOutput);
    	log.info("Number of invalid rdf: " + invalidRdfOutput);
    	log.info("duration in Hours: " + duration.getHours());
    	log.info("duration in Minutes: " + duration.getMinutes());
    	log.info("duration in Seconds: " + duration.getSeconds());
    	log.info("duration in Milliseconds: " + duration.getMillis());
    	
    	
    }  
    
    private static JSAPResult checkArgument(String[] args) throws JSAPException {
    	JSAP jsap = new JSAP();
     
        FlaggedOption opt1 = new FlaggedOption("xmlSourcePathDir")
                                .setStringParser(JSAP.STRING_PARSER)
                                .setRequired(true) 
                                .setShortFlag('i') 
                                .setLongFlag("inputXml");

        opt1.setHelp("Path to cmdi xml input directory.");
        jsap.registerParameter(opt1);
        
        FlaggedOption opt2 = new FlaggedOption("xsltPath")
						        .setStringParser(JSAP.STRING_PARSER)
						        .setRequired(true) 
						        .setShortFlag('x') 
						        .setLongFlag("xsl"); 

		opt2.setHelp("Path to the CMDRecord2RDF file.");
		jsap.registerParameter(opt2);
		
        FlaggedOption opt3 = new FlaggedOption("rdfOutpuDir")
						        .setStringParser(JSAP.STRING_PARSER)
						        .setRequired(true) 
						        .setShortFlag('o') 
						        .setLongFlag("rdfOutput"); 
		
		opt3.setHelp("The directory pathname for rdf output.");
		jsap.registerParameter(opt3);
		
		FlaggedOption opt4 = new FlaggedOption("baseURI")
						        .setStringParser(JSAP.STRING_PARSER)
						        .setRequired(true) 
						        .setShortFlag('b') 
						        .setLongFlag("baseURI"); 

		opt4.setHelp("The base URI.");
		jsap.registerParameter(opt4);
		
		FlaggedOption opt5 = new FlaggedOption("cacheBasePathDir")
								.setStringParser(JSAP.STRING_PARSER)
								.setRequired(false) 
								.setShortFlag('c') 
								.setLongFlag("cache"); 
		
		opt5.setHelp("(Optional) Path to cache dictionary. When this argument is not specified, "
				+ "the '/tmp/cmd2rdf-cache' of in windows will be 'C:/tmp/cmd2rdf-cache' will be used.");
		jsap.registerParameter(opt5);
		
		FlaggedOption opt6 = new FlaggedOption("maxNumberOfFile")
								.setStringParser(JSAP.INTEGER_PARSER)
								.setDefault("0") 
								.setRequired(false) 
								.setShortFlag('n') 
								.setLongFlag("numfile"); 

		opt6.setHelp("(Optional) Max number of xml to be converted.");
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
        return config;
    }

}
