package main.java.com.djrapitops.plan.utilities;

import java.util.HashMap;
import java.util.Map;
import main.java.com.djrapitops.plan.Log;

/**
 *
 * @author Risto
 */
public class Benchmark {
    
    private static Map<String, Long> starts = new HashMap<>();    
    
    /**
     *
     * @param source
     */
    public static void start(String source) {
        starts.put(source, System.nanoTime());
        Log.debug(source);
    }
    
    /**
     *
     * @param source
     * @return
     */
    public static long stop(String source) {
        Long s = starts.get(source);
        if (s != null) {
            long ms = (System.nanoTime() - s) / 1000000;
            Log.debug(source + " took " + ms+" ms");
            starts.remove(source);
            return ms;
        }
        return -1;
    }
}
