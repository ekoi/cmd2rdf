package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.File;
import java.util.List;

import nl.knaw.dans.clarin.cmd2rdf.exception.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerThread implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(WorkerThread.class);
    private String path;
	private List<IAction> actions;
	
	

    public WorkerThread(String path, List<IAction> actions){
        this.path = path;
        this.actions = actions;
    }
    
	public void run() {
    	
    	log.debug("=== run ===");
        try {
			executeActions(path);
		} catch (ActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    private void executeActions(String path) throws ActionException {
			File file = new File(path);
			if (file.exists()) {
				log.debug(file.getName() + " has size of " + file.length() + " bytes (" + (file.length()/1024) + " MB).");
				Object object = file;
				//Do conversion
				for(IAction action : actions) {
					long start = System.currentTimeMillis();
					object = action.execute(path,object);
					long endConv = System.currentTimeMillis();
					log.info("Duration of Conversion: " + ((endConv-start)) + " milliseconds");
				}
			} else {
				log.error("ERROR: '" + path + "' does not exist.");
			}

    }

}
