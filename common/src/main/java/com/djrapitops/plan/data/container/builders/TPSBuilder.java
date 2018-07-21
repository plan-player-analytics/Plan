/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.data.container.builders;


import com.djrapitops.plan.data.container.TPS;

/**
 * Builder for TPS to make it easier to manage.
 *
 * @author Rsl1122
 */
public class TPSBuilder {

    protected long date = 0;
    protected double ticksPerSecond = -1;
    protected int players = -1;
    protected double cpuUsage = -1;
    protected long usedMemory = -1;
    protected int entityCount = -1;
    protected int chunksLoaded = -1;

    /**
     * Hides constructor.
     */
    private TPSBuilder() {
    }

    public static TPSBuilder.Date get() {
        return new TPSBuilder.Chunks();
    }

    public TPS toTPS() {
        return new TPS(date, ticksPerSecond, players, cpuUsage, usedMemory, entityCount, chunksLoaded);
    }

    public static class Date extends TPSBuilder {

        public Ticks date(long date) {
            this.date = date;
            return (Ticks) this;
        }
    }

    public static class Ticks extends Date {

        public Players tps(double tps) {
            ticksPerSecond = tps;
            return (Players) this;
        }

        public Players skipTPS() {
            return (Players) this;
        }
    }

    public static class Players extends Ticks {

        public CPU playersOnline(int online) {
            players = online;
            return (CPU) this;
        }
    }

    public static class CPU extends Players {

        public Memory usedCPU(double cpu) {
            cpuUsage = cpu;
            return (Memory) this;
        }
    }

    public static class Memory extends CPU {

        public Entities usedMemory(long ram) {
            usedMemory = ram;
            return (Entities) this;
        }
    }

    public static class Entities extends Memory {

        public Chunks entities(int count) {
            entityCount = count;
            return (Chunks) this;
        }
    }

    public static class Chunks extends Entities {

        public TPSBuilder chunksLoaded(int chunksLoaded) {
            this.chunksLoaded = chunksLoaded;
            return this;
        }
    }
}
