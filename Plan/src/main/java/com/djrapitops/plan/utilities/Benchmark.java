package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.utilities.BenchUtil;
import com.djrapitops.plugin.utilities.Compatibility;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.bungee.PlanBungee;

/**
 * @author Rsl1122
 */
public class Benchmark {

    /**
     * Constructor used to hide the public constructor
     */
    private Benchmark() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param source
     */
    public static void start(String source) {
        getBenchUtil().start(source);
    }

    /**
     * @param source
     * @return
     */
    public static long stop(String source) {
        long ms = getBenchUtil().stop(source);
        if (ms != -1) {
            Log.debug(source + " took " + ms + " ms");
        }
        return ms;
    }

    /**
     * Used to add Benchmark timings to larger Debug log task parts.
     *
     * @param task   Task this benchmark is a part of.
     * @param source Bench source
     * @return Execution time in ms.
     */
    public static long stop(String task, String source) {
        return getBenchUtil().stop(task, source);
    }

    private static BenchUtil getBenchUtil() {
        if (Compatibility.isBukkitAvailable()) {
            return Plan.getInstance().benchmark();
        } else {
            return PlanBungee.getInstance().benchmark();
        }
    }
}
