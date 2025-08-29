/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.delivery.rendering.json.graphs.line.LineGraph;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.Point;
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.utilities.analysis.Average;
import com.djrapitops.plan.utilities.comparators.TPSComparator;
import com.djrapitops.plan.utilities.java.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Mutator for a list of TPS data.
 * <p>
 * Can be used to get properties of a large number of TPS entries easily.
 *
 * @author AuroraLS3
 */
public class TPSMutator {

    private final List<TPS> tpsData;

    public TPSMutator(List<TPS> tpsData) {
        this.tpsData = tpsData;
    }

    public static TPSMutator copyOf(TPSMutator mutator) {
        return new TPSMutator(new ArrayList<>(mutator.tpsData));
    }

    public TPSMutator filterBy(Predicate<TPS> filter) {
        return new TPSMutator(Lists.filter(tpsData, filter));
    }

    public TPSMutator filterDataBetween(long after, long before) {
        return filterBy(tps -> tps.getDate() >= after && tps.getDate() <= before);
    }

    public TPSMutator filterTPSBetween(double above, double below) {
        return filterBy(tps -> tps.getTicksPerSecond() > above && tps.getTicksPerSecond() < below);
    }

    public List<TPS> all() {
        return tpsData;
    }

    public List<Point> playersOnlinePoints() {
        return Lists.map(tpsData, tps -> new Point(tps.getDate(), tps.getPlayers()));
    }

    public List<Point> tpsPoints() {
        return Lists.map(tpsData, tps -> new Point(tps.getDate(), tps.getTicksPerSecond()));
    }

    public List<Point> cpuPoints() {
        return tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getCPUUsage()))
                .filter(point -> point.getY() != -1)
                .collect(Collectors.toList());
    }

    public List<Point> ramUsagePoints() {
        return Lists.map(tpsData, tps -> new Point(tps.getDate(), tps.getUsedMemory()));
    }

    public List<Point> entityPoints() {
        return Lists.map(tpsData, tps -> new Point(tps.getDate(), tps.getEntityCount()));
    }

    public List<Point> chunkPoints() {
        return Lists.map(tpsData, tps -> new Point(tps.getDate(), tps.getChunksLoaded()));
    }

    public List<Point> freeDiskPoints() {
        return tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getFreeDiskSpace()))
                .filter(point -> point.getY() != -1)
                .collect(Collectors.toList());
    }

    public long serverDownTime() {
        long lastDate = -1;
        long downTime = 0;
        tpsData.sort(new TPSComparator());
        for (TPS tps : tpsData) {
            long date = tps.getDate();
            if (lastDate == -1) {
                lastDate = date;
                continue;
            }

            long diff = date - lastDate;
            if (diff > TimeUnit.MINUTES.toMillis(3L)) {
                downTime += diff;
            }
            lastDate = date;
        }

        return downTime;
    }

    public long serverUptime() {
        long lastDate = -1;
        long uptime = 0;
        tpsData.sort(new TPSComparator());

        for (TPS tps : tpsData) {
            long date = tps.getDate();
            if (lastDate == -1) {
                lastDate = date;
                continue;
            }

            long diff = date - lastDate;
            if (diff < TimeUnit.MINUTES.toMillis(2L)) {
                uptime += diff;
            }
            lastDate = date;
        }

        return uptime;
    }

    public long serverOccupiedTime() {
        long lastDate = -1;
        long activeTime = 0;
        tpsData.sort(new TPSComparator());
        for (TPS tps : tpsData) {
            long date = tps.getDate();
            if (lastDate == -1) {
                lastDate = date;
                continue;
            }

            int players = tps.getPlayers();
            long diff = date - lastDate;
            if (players > 0 && diff <= TimeUnit.MINUTES.toMillis(3L)) {
                activeTime += diff;
            }

            lastDate = date;
        }

        return activeTime;
    }

    public double percentageTPSAboveThreshold(int threshold) {
        if (tpsData.isEmpty()) {
            return 1;
        }

        long count = 0;
        for (TPS tps : tpsData) {
            if (tps.getTicksPerSecond() >= threshold) {
                count++;
            }
        }

        return count * 1.0 / tpsData.size();
    }

    public int lowTpsSpikeCount(double threshold) {
        boolean wasLow = false;
        int spikeCount = 0;

        for (TPS tpsObj : tpsData) {
            double tps = tpsObj.getTicksPerSecond();
            if (0 <= tps && tps < threshold) {
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

    public int averagePlayers() {
        return (int) tpsData.stream()
                .mapToInt(TPS::getPlayers)
                .filter(num -> num >= 0)
                .average().orElse(-1);
    }

    public double averageTPS() {
        return tpsData.stream()
                .mapToDouble(TPS::getTicksPerSecond)
                .filter(num -> num >= 0)
                .average().orElse(-1);
    }

    public double averageCPU() {
        return tpsData.stream()
                .mapToDouble(TPS::getCPUUsage)
                .filter(num -> num >= 0)
                .average().orElse(-1);
    }

    public double averageRAM() {
        return tpsData.stream()
                .mapToDouble(TPS::getUsedMemory)
                .filter(num -> num >= 0)
                .average().orElse(-1);
    }

    public double averageEntities() {
        return tpsData.stream()
                .mapToDouble(TPS::getEntityCount)
                .filter(num -> num >= 0)
                .average().orElse(-1);
    }

    public double averageChunks() {
        return tpsData.stream()
                .mapToDouble(TPS::getChunksLoaded)
                .filter(num -> num >= 0)
                .average().orElse(-1);
    }

    public double averageFreeDisk() {
        return tpsData.stream()
                .mapToDouble(TPS::getFreeDiskSpace)
                .filter(num -> num >= 0)
                .average().orElse(-1);
    }

    public long maxFreeDisk() {
        return tpsData.stream()
                .mapToLong(TPS::getFreeDiskSpace)
                .filter(num -> num >= 0)
                .max().orElse(-1);
    }

    public long minFreeDisk() {
        return tpsData.stream()
                .mapToLong(TPS::getFreeDiskSpace)
                .filter(num -> num >= 0)
                .min().orElse(-1);
    }

    public double averagePlayersOnline() {
        return tpsData.stream()
                .mapToDouble(TPS::getPlayers)
                .average().orElse(-1);
    }

    public Optional<TPS> getLast() {
        if (tpsData.isEmpty()) return Optional.empty();
        // else
        tpsData.sort(new TPSComparator());
        return Optional.of(tpsData.get(tpsData.size() - 1));
    }

    public List<Number[]> toArrays(LineGraph.GapStrategy gapStrategy) {
        List<Number[]> arrays = new ArrayList<>();
        Long lastX = null;
        for (TPS tps : tpsData) {
            long date = tps.getDate();
            if (gapStrategy.fillGaps && lastX != null && date - lastX > gapStrategy.acceptableGapMs) {
                addMissingPoints(arrays, lastX, date, gapStrategy);
            }
            lastX = date;

            arrays.add(tps.toArray());
        }
        return arrays;
    }

    private void addMissingPoints(List<Number[]> arrays, Long lastX, long date, LineGraph.GapStrategy gapStrategy) {
        long iterate = lastX + gapStrategy.diffToFirstGapPointMs;
        while (iterate < date) {
            Number[] entry = new Number[7];
            if (gapStrategy.fillWith != null) Arrays.fill(entry, gapStrategy.fillWith);
            entry[0] = iterate;
            arrays.add(entry);
            iterate += gapStrategy.fillFrequencyMs;
        }
    }

    public double averageMspt() {
        Average average = new Average();
        for (TPS tps : tpsData) {
            average.addNonNull(tps.getMsptAverage());
        }
        return average.getAverageAndReset();
    }
}