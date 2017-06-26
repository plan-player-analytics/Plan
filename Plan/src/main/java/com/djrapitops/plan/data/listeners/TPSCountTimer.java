package main.java.com.djrapitops.plan.data.listeners;

import com.djrapitops.javaplugin.task.RslBukkitRunnable;
import java.util.ArrayList;
import java.util.List;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

/**
 * Class responsible for calculating TPS every second.
 *
 * @author Rsl1122
 */
public class TPSCountTimer extends RslBukkitRunnable<Plan> {

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
            return;
        }
        TPS tps = calculateTPS(diff, now);
        history.add(tps);
        if (history.size() >= 60) {
            handler.addTPSLastMinute(history);
            history.clear();
        }
    }

    public TPS calculateTPS(long diff, long now) {
        if (diff < 1000000000L) { // No tick count above 20
            diff = 1000000000L; // 1 000 000 000ns = 1s
        }
        int playersOnline = plugin.getServer().getOnlinePlayers().size();
        while (diff > 20000000000L) {
            history.add(new TPS(now, 0, playersOnline));
            diff -= 20000000000L;
        }
        double tpsN = 20000000000L / diff; // 20 000 000 000ns
        
        TPS tps = new TPS(now, tpsN, playersOnline);
        return tps;
    }
}
