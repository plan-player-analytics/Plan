package main.java.com.djrapitops.plan.utilities;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;

/**
 * @author Rsl1122
 */
public class Benchmark {

    /**
     * @param source
     */
    public static void start(String source) {
        Plan.getInstance().benchmark().start(source);
        Log.debug(source);
    }

    /**
     * @param source
     * @return
     */
    public static long stop(String source) {
        long ms = Plan.getInstance().benchmark().stop(source);
        if (ms != -1) {
            Log.debug(source + " took " + ms + " ms");
        }
        return ms;
    }
}
