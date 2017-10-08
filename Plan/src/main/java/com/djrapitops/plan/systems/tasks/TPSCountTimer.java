package main.java.com.djrapitops.plan.systems.tasks;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Compatibility;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.systems.processing.TPSInsertProcessor;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import org.bukkit.World;

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

    private final IPlan plugin;
    private final List<TPS> history;
    private long lastCheckNano;

    private final boolean usingBungee;

    private int latestPlayersOnline = 0;

    public TPSCountTimer(IPlan plugin) {
        super("TPSCountTimer");
        lastCheckNano = -1;
        this.plugin = plugin;
        history = new ArrayList<>();
        usingBungee = Compatibility.isBungeeAvailable();
    }

    @Override
    public void run() {
        long nanoTime = System.nanoTime();
        long now = MiscUtils.getTime();

        if (usingBungee) {
            history.add(new TPS(now, -1, ((PlanBungee) plugin).getProxy().getOnlineCount(), -1, -1, -1, -1));
        } else {
            long diff = nanoTime - lastCheckNano;

            lastCheckNano = nanoTime;

            if (diff > nanoTime) { // First run's diff = nanoTime + 1, no calc possible.
                Log.debug("First run of TPSCountTimer Task.");
                return;
            }

            history.add(calculateTPS(diff, now));
        }
        if (history.size() >= 60) {
            plugin.addToProcessQueue(new TPSInsertProcessor(new ArrayList<>(history)));
            history.clear();
        }
    }

    /**
     * Calculates the TPS
     *
     * @param diff The time difference between the last run and the new run
     * @param now  The time right now
     * @return the TPS
     */
    private TPS calculateTPS(long diff, long now) {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        int availableProcessors = operatingSystemMXBean.getAvailableProcessors();
        double averageCPUUsage = MathUtils.round(operatingSystemMXBean.getSystemLoadAverage() / availableProcessors * 100.0);

        if (averageCPUUsage < 0) { // If unavailable, getSystemLoadAverage() returns -1
            averageCPUUsage = -1;
        }

        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long usedMemory = (totalMemory - runtime.freeMemory()) / 1000000;

        int playersOnline = ((Plan) plugin).getServer().getOnlinePlayers().size();
        latestPlayersOnline = playersOnline;
        int loadedChunks = getLoadedChunks();
        int entityCount;

        if (plugin.getVariable().isUsingPaper()) {
            entityCount = getEntityCountPaper();

            return getTPSPaper(now, averageCPUUsage, usedMemory, entityCount, loadedChunks, playersOnline);
        } else {
            entityCount = getEntityCount();

            // 40ms removed because the run appears to take 40-50ms, screwing the tps.
            long fortyMsAsNs = TimeAmount.MILLISECOND.ns() * 40L;
            return getTPS(diff - fortyMsAsNs, now, averageCPUUsage, usedMemory, entityCount, loadedChunks, playersOnline);
        }
    }

    /**
     * Gets the TPS for Spigot / Bukkit
     *
     * @param diff          The difference between the last run and this run
     * @param now           The time right now
     * @param cpuUsage      The usage of the CPU
     * @param playersOnline The amount of players that are online
     * @return the TPS
     */
    private TPS getTPS(long diff, long now, double cpuUsage, long usedMemory, int entityCount, int chunksLoaded, int playersOnline) {
        long difference = diff;
        if (difference < TimeAmount.SECOND.ns()) { // No tick count above 20
            difference = TimeAmount.SECOND.ns();
        }

        long twentySeconds = 20L * TimeAmount.SECOND.ns();
        while (difference > twentySeconds) {
            history.add(new TPS(now, 0, playersOnline, cpuUsage, usedMemory, entityCount, chunksLoaded));
            difference -= twentySeconds;
        }

        double tpsN = twentySeconds * 1.0 / difference;

        return new TPS(now, tpsN, playersOnline, cpuUsage, usedMemory, entityCount, chunksLoaded);
    }

    /**
     * Gets the TPS for Paper
     *
     * @param now           The time right now
     * @param cpuUsage      The usage of the CPU
     * @param playersOnline The amount of players that are online
     * @return the TPS
     */
    private TPS getTPSPaper(long now, double cpuUsage, long usedMemory, int entityCount, int chunksLoaded, int playersOnline) {
        double tps = ((Plan) plugin).getServer().getTPS()[0];

        if (tps > 20) {
            tps = 20;
        }

        tps = MathUtils.round(tps);

        return new TPS(now, tps, playersOnline, cpuUsage, usedMemory, entityCount, chunksLoaded);
    }

    /**
     * Gets the amount of loaded chunks
     *
     * @return amount of loaded chunks
     */
    private int getLoadedChunks() {
        return ((Plan) plugin).getServer().getWorlds().stream().mapToInt(world -> world.getLoadedChunks().length).sum();
    }

    /**
     * Gets the amount of entities on the server for Bukkit / Spigot
     *
     * @return amount of entities
     */
    private int getEntityCount() {
        return ((Plan) plugin).getServer().getWorlds().stream().mapToInt(world -> world.getEntities().size()).sum();
    }

    /**
     * Gets the amount of entities on the server for Paper
     *
     * @return amount of entities
     */
    private int getEntityCountPaper() {
        return ((Plan) plugin).getServer().getWorlds().stream().mapToInt(World::getEntityCount).sum();
    }

    public int getLatestPlayersOnline() {
        return latestPlayersOnline;
    }
}
