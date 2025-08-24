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
package com.djrapitops.plan.gathering.domain;

import com.djrapitops.plan.delivery.domain.DateHolder;

import java.util.Objects;

/**
 * Class containing single datapoint of TPS / Players online / CPU Usage / Used Memory / Entity Count / Chunks loaded.
 *
 * @author AuroraLS3
 */
public class TPS implements DateHolder {

    private final long date;
    private final double ticksPerSecond;
    private final int players;
    private final double cpuUsage;
    private final long usedMemory;
    private final int entityCount;
    private final int chunksLoaded;
    private final long freeDiskSpace;
    private Double averageMspt;
    private Double mspt95thPercentile;

    /**
     * Constructor.
     *
     * @param date           time of the TPS calculation.
     * @param ticksPerSecond average ticksPerSecond for the last minute.
     * @param players        players for the minute.
     * @param cpuUsage       CPU usage for the minute
     * @param usedMemory     used memory (megabytes) at the time of fetching
     * @param entityCount    amount of entities at the time of fetching
     * @param chunksLoaded   amount of chunks loaded at the time of fetching
     * @param freeDiskSpace  free megabytes in the partition the server is running in.
     */
    public TPS(
            long date,
            double ticksPerSecond,
            int players,
            double cpuUsage,
            long usedMemory,
            int entityCount,
            int chunksLoaded,
            long freeDiskSpace
    ) {
        this.date = date;
        this.ticksPerSecond = ticksPerSecond;
        this.players = players;
        this.cpuUsage = cpuUsage;
        this.usedMemory = usedMemory;
        this.entityCount = entityCount;
        this.chunksLoaded = chunksLoaded;
        this.freeDiskSpace = freeDiskSpace;
    }

    @Override
    public long getDate() {
        return date;
    }

    /**
     * Get the average ticksPerSecond for the minute.
     *
     * @return 0-20 double
     */
    public double getTicksPerSecond() {
        return ticksPerSecond;
    }

    /**
     * Get the player for the time, when the data was fetched.
     *
     * @return Players online.
     */
    public int getPlayers() {
        return players;
    }

    /**
     * Get the average CPU Usage for the minute
     *
     * @return 0-100 double
     */
    public double getCPUUsage() {
        return Double.isNaN(cpuUsage) ? -1.0 : cpuUsage;
    }

    /**
     * Get the used memory for the time, when the data was fetched.
     *
     * @return Used Memory in Megabyte
     */
    public long getUsedMemory() {
        return usedMemory;
    }

    /**
     * Get the amount of entities for the time, when the data was fetched
     *
     * @return Amount of entities
     */
    public int getEntityCount() {
        return entityCount;
    }

    /**
     * Get the amount of chunks loaded for the time, when the data was fetched
     *
     * @return Amount of chunks loaded
     */
    public int getChunksLoaded() {
        return chunksLoaded;
    }

    /**
     * Get free megabytes of disk space on the server disk.
     *
     * @return Amount of free megabytes available on disk.
     */
    public long getFreeDiskSpace() {
        return freeDiskSpace;
    }

    public Double getAverageMspt() {
        return averageMspt;
    }

    public void setAverageMspt(Double averageMspt) {
        this.averageMspt = averageMspt;
    }

    public Double getMspt95thPercentile() {
        return mspt95thPercentile;
    }

    public void setMspt95thPercentile(Double mspt95thPercentile) {
        this.mspt95thPercentile = mspt95thPercentile;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TPS tps = (TPS) o;
        return getDate() == tps.getDate() && Double.compare(getTicksPerSecond(), tps.getTicksPerSecond()) == 0 && getPlayers() == tps.getPlayers() && Double.compare(cpuUsage, tps.cpuUsage) == 0 && getUsedMemory() == tps.getUsedMemory() && getEntityCount() == tps.getEntityCount() && getChunksLoaded() == tps.getChunksLoaded() && getFreeDiskSpace() == tps.getFreeDiskSpace() && Objects.equals(getAverageMspt(), tps.getAverageMspt()) && Objects.equals(getMspt95thPercentile(), tps.getMspt95thPercentile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getTicksPerSecond(), getPlayers(), cpuUsage, getUsedMemory(), getEntityCount(), getChunksLoaded(), getFreeDiskSpace(), getAverageMspt(), getMspt95thPercentile());
    }

    @Override
    public String toString() {
        return "TPS{" +
                "date=" + date +
                ", ticksPerSecond=" + ticksPerSecond +
                ", players=" + players +
                ", cpuUsage=" + cpuUsage +
                ", usedMemory=" + usedMemory +
                ", entityCount=" + entityCount +
                ", chunksLoaded=" + chunksLoaded +
                ", freeDiskSpace=" + freeDiskSpace +
                ", averageMspt=" + averageMspt +
                ", mspt95thPercentile=" + mspt95thPercentile +
                '}';
    }

    public Number[] toArray() {
        double tps = getTicksPerSecond();
        double cpu = getCPUUsage();
        long ram = getUsedMemory();
        int entities = getEntityCount();
        int chunks = getChunksLoaded();
        long disk = getFreeDiskSpace();
        return new Number[]{
                getDate(),
                getPlayers(),
                tps >= 0 ? tps : null,
                cpu >= 0 ? cpu : null,
                ram >= 0 ? ram : null,
                entities >= 0 ? entities : null,
                chunks >= 0 ? chunks : null,
                disk >= 0 ? disk : null
        };
    }
}
