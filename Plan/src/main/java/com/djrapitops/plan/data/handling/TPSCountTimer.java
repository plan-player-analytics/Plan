package main.java.com.djrapitops.plan.data.handling;

import com.djrapitops.javaplugin.task.RslBukkitRunnable;
import java.util.ArrayList;
import java.util.List;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;

/**
 * Class responsible for calculating TPS every second.
 *
 * @author Rsl1122
 */
public class TPSCountTimer extends RslBukkitRunnable<Plan> {

    private long lastCheck;
    private final Plan plugin;
    private final DataCacheHandler handler;
    private final List<TPS> history;

    public TPSCountTimer(Plan plugin) {
        super("TPSCountTimer");
        lastCheck = -1;
        this.handler = plugin.getHandler();
        this.plugin = plugin;
        history = new ArrayList<>();
    }

    @Override
    public void run() {
        long now = System.nanoTime();
        long diff = now - lastCheck;
        lastCheck = now;
        if (diff > now) { // First run's diff = now + 1, no calc possible.
            return;
        }
        TPS tps = calculateTPS(diff, now);
        history.add(tps);
        if (history.size() >= 60) {
            handler.addTPSLastMinute(history);
            history.clear();
        }
    }

    public TPS calculateTPS(long diff, long tpsDate) {
        long expectedDiff = 1000000000L; // 1 000 000 000 ns / 1 s
        long difference = diff - expectedDiff;
        if (difference < 1000000) { // If less than 1 millisecond it is forgiven.
            difference = 0;
        }
        double tpsN = 20 - ((difference / expectedDiff) * 20);
        int playersOnline = plugin.getServer().getOnlinePlayers().size();
        TPS tps = new TPS(tpsDate / 1000000L, tpsN, playersOnline);
        return tps;
    }
}
