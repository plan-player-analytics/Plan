/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.utilities.comparators.TPSComparator;
import com.djrapitops.plan.utilities.html.graphs.line.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
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

    public TPSMutator filterBy(Predicate<TPS> filter) {
        return new TPSMutator(tpsData.stream()
                .filter(filter)
                .collect(Collectors.toList()));
    }

    public TPSMutator filterDataBetween(long after, long before) {
        return filterBy(tps -> tps.getDate() >= after && tps.getDate() <= before);
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
                .filter(point -> point.getY() != -1)
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

    public long serverIdleTime() {
        long lastDate = -1;
        int lastPlayers = 0;
        long idleTime = 0;
        tpsData.sort(new TPSComparator());
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

    public int lowTpsSpikeCount(int threshold) {
        boolean wasLow = false;
        int spikeCount = 0;

        for (TPS tpsObj : tpsData) {
            double tps = tpsObj.getTicksPerSecond();
            if (tps < threshold) {
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