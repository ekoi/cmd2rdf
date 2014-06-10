package nl.knaw.dans.clarin.playground;

import java.util.concurrent.ConcurrentHashMap;

public class WorkerThread implements Runnable {

    private int command;
    private ConcurrentHashMap<String, Boolean> map;
    public WorkerThread(int s, ConcurrentHashMap<String, Boolean> map){

        this.command=s;
        this.map = map;
    }


    public void run() {
    	//System.out.println("============== COUNT: " + count);
        System.out.println(Thread.currentThread().getName()+" \tStart. Command = "+command + "\tsize:" + map.size());

        processCommand();

        System.out.println(Thread.currentThread().getName()+" End.");

    }

 

    private void processCommand() {

        try {
        	if (map.containsKey("eko1")){
        		System.out.println("====" + map.get("eko1"));
        	} else {
        		System.out.println("---------- create new one ---------- by: " + Thread.currentThread().getName());
        		map.putIfAbsent("eko1", false);
        		boolean b = map.contains("eko1");
        		System.out.println(b);
        	}
            Thread.sleep(1000*command);
          
            map.putIfAbsent("eko2", true);

        } catch (InterruptedException e) {

            e.printStackTrace();

        }

    }

 


}
