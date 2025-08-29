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
package com.djrapitops.plan.gathering.domain.builders;

import com.djrapitops.plan.gathering.domain.TPS;

/**
 * Builder for TPS to make it easier to manage.
 *
 * @author AuroraLS3
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
    protected Double msptAverage = null;
    protected Double mspt95thPercentile = null;

    /**
     * Hides constructor.
     */
    private TPSBuilder() {
    }

    public static TPSBuilder get() {
        return new TPSBuilder();
    }

    public TPS toTPS() {
        TPS tps = new TPS(date, ticksPerSecond, players, cpuUsage, usedMemory, entityCount, chunksLoaded, freeDiskSpace);
        tps.setMsptAverage(msptAverage);
        tps.setMspt95thPercentile(mspt95thPercentile);
        return tps;
    }

    public TPSBuilder date(long date) {
        this.date = date;
        return this;
    }

    public TPSBuilder tps(double tps) {
        ticksPerSecond = tps;
        return this;
    }

    public TPSBuilder playersOnline(int online) {
        players = online;
        return this;
    }

    public TPSBuilder usedCPU(double cpu) {
        cpuUsage = cpu;
        return this;
    }

    public TPSBuilder usedMemory(long ram) {
        usedMemory = ram;
        return this;
    }

    public TPSBuilder entities(int count) {
        entityCount = count;
        return this;
    }

    public TPSBuilder chunksLoaded(int chunksLoaded) {
        this.chunksLoaded = chunksLoaded;
        return this;
    }

    public TPSBuilder freeDiskSpace(long freeDiskSpace) {
        this.freeDiskSpace = freeDiskSpace;
        return this;
    }

    public TPSBuilder msptAverage(Double msptAverage) {
        this.msptAverage = msptAverage;
        return this;
    }

    public TPSBuilder msptAverage(double msptAverage, boolean wasNull) {
        if (!wasNull) {
            this.msptAverage = msptAverage;
        }
        return this;
    }

    public TPSBuilder mspt95thPercentile(Double mspt95thPercentile) {
        this.mspt95thPercentile = mspt95thPercentile;
        return this;
    }

    public TPSBuilder mspt95thPercentile(double mspt95thPercentile, boolean wasNull) {
        if (!wasNull) {
            this.mspt95thPercentile = mspt95thPercentile;
        }
        return this;
    }
}
