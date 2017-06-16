package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.javaplugin.utilities.BenchmarkUtil;
import main.java.com.djrapitops.plan.Log;

/**
 *
 * @author Rsl1122
 */
public class Benchmark {
    
    /**
     *
     * @param source
     */
    public static void start(String source) {
        BenchmarkUtil.start(source);
        Log.debug(source);
    }
    
    /**
     *
     * @param source
     * @return
     */
    public static long stop(String source) {
        long ms = BenchmarkUtil.stop(source);
        if (ms != -1) {
            Log.debug(source + " took " + ms+" ms");
        }
        return ms;
    }
}
