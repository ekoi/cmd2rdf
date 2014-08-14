/**
 * 
 */
package nl.knaw.dans.clarin.playground;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author akmi
 *
 */
public class SimpleThreadPool {
	public static void main(String[] args) {
		int count=0;
		ConcurrentHashMap<String, Boolean> map = new ConcurrentHashMap<String, Boolean>();
		//map.putIfAbsent("eko1", true);
		System.out.println(map.containsKey("eko1"));
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 1; i < 11; i++) {
            Runnable worker = new WorkerThread(i, map);
            executor.execute(worker);
            count++;
          }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }
}
