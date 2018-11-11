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
    protected long freeDiskSpace = -1;

    /**
     * Hides constructor.
     */
    private TPSBuilder() {
    }

    public static TPSBuilder.Date get() {
        return new TPSBuilder.DiskSpace();
    }

    public TPS toTPS() {
        return new TPS(date, ticksPerSecond, players, cpuUsage, usedMemory, entityCount, chunksLoaded, freeDiskSpace);
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

        public DiskSpace chunksLoaded(int chunksLoaded) {
            this.chunksLoaded = chunksLoaded;
            return (DiskSpace) this;
        }
    }

    public static class DiskSpace extends Chunks {
        public TPSBuilder freeDiskSpace(long freeDiskSpace) {
            this.freeDiskSpace = freeDiskSpace;
            return this;
        }
    }
}
