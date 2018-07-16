package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.html.graphs.line.Point;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Mutator for a list of TPS data.
 * <p>
 * Can be used to get properties of a large number of TPS entries easily.
 *
 * @author Rsl1122
 */
public class TPSMutator {

    private List<TPS> tpsData;

    public TPSMutator(List<TPS> tpsData) {
        this.tpsData = tpsData;
    }

    public static TPSMutator forContainer(DataContainer container) {
        return new TPSMutator(container.getValue(ServerKeys.TPS).orElse(new ArrayList<>()));
    }

    public static TPSMutator copyOf(TPSMutator mutator) {
        return new TPSMutator(new ArrayList<>(mutator.tpsData));
    }

    public TPSMutator filterDataBetween(long after, long before) {
        return new TPSMutator(tpsData.stream()
                .filter(tps -> tps.getDate() >= after && tps.getDate() <= before)
                .collect(Collectors.toList()));
    }

    public List<TPS> all() {
        return tpsData;
    }

    public List<Point> playersOnlinePoints() {
        return tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getPlayers()))
                .collect(Collectors.toList());
    }

    public List<Point> tpsPoints() {
        return tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getTicksPerSecond()))
                .collect(Collectors.toList());
    }

    public List<Point> cpuPoints() {
        return tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getCPUUsage()))
                .collect(Collectors.toList());
    }

    public List<Point> ramUsagePoints() {
        return tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getUsedMemory()))
                .collect(Collectors.toList());
    }

    public List<Point> entityPoints() {
        return tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getEntityCount()))
                .collect(Collectors.toList());
    }

    public List<Point> chunkPoints() {
        return tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getChunksLoaded()))
                .collect(Collectors.toList());
    }

    public long serverDownTime() {
        long lastDate = -1;
        long downTime = 0;
        for (TPS tps : tpsData) {
            long date = tps.getDate();
            if (lastDate == -1) {
                lastDate = date;
                continue;
            }

            long diff = date - lastDate;
            if (diff > TimeAmount.MINUTE.ms() * 3L) {
                downTime += diff;
            }
            lastDate = date;
        }

        return downTime;
    }

    public long serverIdleTime() {
        long lastDate = -1;
        int lastPlayers = 0;
        long idleTime = 0;
        for (TPS tps : tpsData) {
            long date = tps.getDate();
            int players = tps.getPlayers();
            if (lastDate == -1) {
                lastDate = date;
                lastPlayers = players;
                continue;
            }

            long diff = date - lastDate;
            if (lastPlayers == 0 && players == 0) {
                idleTime += diff;
            }

            lastDate = date;
            lastPlayers = players;
        }

        return idleTime;
    }

    public double percentageTPSAboveLowThreshold() {
        if (tpsData.isEmpty()) {
            return 1;
        }

        int threshold = Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber();

        long count = 0;
        for (TPS tps : tpsData) {
            if (tps.getTicksPerSecond() >= threshold) {
                count++;
            }
        }

        return count * 1.0 / tpsData.size();
    }

    public int lowTpsSpikeCount() {
        int mediumThreshold = Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber();

        boolean wasLow = false;
        int spikeCount = 0;

        for (TPS tpsObj : tpsData) {
            double tps = tpsObj.getTicksPerSecond();
            if (tps < mediumThreshold) {
                if (!wasLow) {
                    spikeCount++;
                    wasLow = true;
                }
            } else {
                wasLow = false;
            }
        }

        return spikeCount;
    }

    public double averageTPS() {
        OptionalDouble average = tpsData.stream()
                .mapToDouble(TPS::getTicksPerSecond)
                .filter(num -> num >= 0)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public double averageCPU() {
        OptionalDouble average = tpsData.stream()
                .mapToDouble(TPS::getCPUUsage)
                .filter(num -> num >= 0)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public double averageRAM() {
        OptionalDouble average = tpsData.stream()
                .mapToDouble(TPS::getUsedMemory)
                .filter(num -> num >= 0)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public double averageEntities() {
        OptionalDouble average = tpsData.stream()
                .mapToDouble(TPS::getEntityCount)
                .filter(num -> num >= 0)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }

    public double averageChunks() {
        OptionalDouble average = tpsData.stream()
                .mapToDouble(TPS::getChunksLoaded)
                .filter(num -> num >= 0)
                .average();
        if (average.isPresent()) {
            return average.getAsDouble();
        }
        return -1;
    }
}