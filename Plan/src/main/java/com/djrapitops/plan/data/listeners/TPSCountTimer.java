package main.java.com.djrapitops.plan.data.listeners;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for calculating TPS every second.
 *
 * @author Rsl1122
 */
public class TPSCountTimer extends AbsRunnable {

    private long lastCheckNano;
    private final Plan plugin;
    private final DataCacheHandler handler;
    private final List<TPS> history;

    public TPSCountTimer(Plan plugin) {
        super("TPSCountTimer");
        lastCheckNano = -1;
        this.handler = plugin.getHandler();
        this.plugin = plugin;
        history = new ArrayList<>();
    }

    @Override
    public void run() {
        long nanoTime = System.nanoTime();
        long now = MiscUtils.getTime();
        long diff = nanoTime - lastCheckNano;

        lastCheckNano = nanoTime;

        if (diff > nanoTime) { // First run's diff = nanoTime + 1, no calc possible.
            Log.debug("First run of TPSCountTimer Task.");
            return;
        }

        TPS tps = calculateTPS(diff, now);
        history.add(tps);

        if (history.size() >= 60) {
            handler.addTPSLastMinute(history);
            history.clear();
        }
    }

    /**
     * Calculates the TPS
     *
     * @param diff The time difference between the last run and the new run
     * @param now The time right now
     * @return
     */
    private TPS calculateTPS(long diff, long now) {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        final double averageCPUUsage = MathUtils.round(operatingSystemMXBean.getSystemLoadAverage() / availableProcessors * 100.0);

        int playersOnline = plugin.getServer().getOnlinePlayers().size();

        if (plugin.getVariable().isUsingPaper()) {
            return getTPSPaper(now, averageCPUUsage, playersOnline);
        } else {
            diff -= TimeAmount.MILLISECOND.ns() * 40L; // 40ms removed because the run appears to take 40-50ms, screwing the tps.
            return getTPS(diff, now, averageCPUUsage, playersOnline);
        }
    }

    private TPS getTPSPaper(long now, double cpuUsage, int playersOnline) {
        double tps = plugin.getServer().getTPS()[0];

        if (tps > 20) {
            tps = 20;
        }

        tps = MathUtils.round(tps);

        return new TPS(now, tps, playersOnline, cpuUsage);
    }

    private TPS getTPS(long diff, long now, double cpuUsage, int playersOnline) {
        if (diff < TimeAmount.SECOND.ns()) { // No tick count above 20
            diff = TimeAmount.SECOND.ns();
        }

        long twentySeconds = 20L * TimeAmount.SECOND.ns();
        while (diff > twentySeconds) {
            history.add(new TPS(now, 0, playersOnline, cpuUsage));
            diff -= twentySeconds;
        }

        double tpsN = twentySeconds / diff;

        return new TPS(now, tpsN, playersOnline, cpuUsage);
    }
}
