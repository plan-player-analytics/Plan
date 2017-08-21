/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data;

/**
 * Class containing single datapoint of TPS / Players online / CPU Usage / Used Memory / Entity Count / Chunks loaded.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class TPS {

    private final long date;
    private final double ticksPerSecond;
    private final int players;
    private final double cpuUsage;
    private final long usedMemory;
    private final int entityCount;
    private final int chunksLoaded;

    /**
     * Constructor.
     *
     * @param date         time of the TPS calculation.
     * @param ticksPerSecond          average ticksPerSecond for the last minute.
     * @param players      players for the minute.
     * @param cpuUsage     CPU usage for the minute
     * @param usedMemory   used memory at the time of fetching
     * @param entityCount  amount of entities at the time of fetching
     * @param chunksLoaded amount of chunks loaded at the time of fetching
     */
    public TPS(long date, double ticksPerSecond, int players, double cpuUsage, long usedMemory, int entityCount, int chunksLoaded) {
        this.date = date;
        this.ticksPerSecond = ticksPerSecond;
        this.players = players;
        this.cpuUsage = cpuUsage;
        this.usedMemory = usedMemory;
        this.entityCount = entityCount;
        this.chunksLoaded = chunksLoaded;
    }

    /**
     * Get the time of the average calculation.
     *
     * @return epoch ms.
     */
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
        return cpuUsage;
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.date ^ (this.date >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.ticksPerSecond) ^ (Double.doubleToLongBits(this.ticksPerSecond) >>> 32));
        hash = 97 * hash + this.players;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final TPS other = (TPS) obj;
        return date == other.date
                && Double.compare(this.ticksPerSecond, other.ticksPerSecond) == 0
                && this.players == other.players
                && Double.compare(cpuUsage, other.cpuUsage) == 0;
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
                '}';
    }
}
