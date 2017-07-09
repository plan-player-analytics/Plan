package main.java.com.djrapitops.plan.data.listeners;

import com.djrapitops.javaplugin.api.TimeAmount;
import com.djrapitops.javaplugin.task.runnable.RslRunnable;
import java.util.ArrayList;
import java.util.List;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

/**
 * Class responsible for calculating TPS every second.
 *
 * @author Rsl1122
 */
public class TPSCountTimer extends RslRunnable {

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
        long nanotime = System.nanoTime();
        long now = MiscUtils.getTime();
        long diff = nanotime - lastCheckNano;
        lastCheckNano = nanotime;
        if (diff > nanotime) { // First run's diff = nanotime + 1, no calc possible.
            Log.debug("First run of TPSCountTimer Task.");
            return;
        }
        diff -= TimeAmount.MILLISECOND.ns() * 40L; // 40ms Removed because the run appears to take 40-50ms, scewing the tps.
        TPS tps = calculateTPS(diff, now);
        history.add(tps);
        if (history.size() >= 60) {
            handler.addTPSLastMinute(history);
            history.clear();
        }
    }

    public TPS calculateTPS(long diff, long now) {
        if (diff < TimeAmount.SECOND.ns()) { // No tick count above 20
            diff = TimeAmount.SECOND.ns();
        }
        int playersOnline = plugin.getServer().getOnlinePlayers().size();
        long twentySeconds = 20L * TimeAmount.SECOND.ns();
        while (diff > twentySeconds) {
            history.add(new TPS(now, 0, playersOnline));
            diff -= twentySeconds;
        }
        double tpsN = twentySeconds / diff;

        TPS tps = new TPS(now, tpsN, playersOnline);
        return tps;
    }
}
