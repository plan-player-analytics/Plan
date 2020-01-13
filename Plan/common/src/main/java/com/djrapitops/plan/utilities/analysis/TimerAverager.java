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
package com.djrapitops.plan.utilities.analysis;

import java.util.concurrent.TimeUnit;

/**
 * Utility for averaging time based data.
 *
 * @author Rsl1122
 */
public class TimerAverager {

    private long savePeriodMs;
    private long lastSaveMs;

    private double total;
    private int count;

    public TimerAverager() {
        this(TimeUnit.MINUTES.toMillis(1L));
    }

    public TimerAverager(long savePeriodMs) {
        this.savePeriodMs = savePeriodMs;
        lastSaveMs = 0;

        total = 0.0;
        count = 0;
    }

    /**
     * Add a new entry and check if save should be done.
     *
     * @param value TPS value
     * @return If a save should be performed.
     */
    public boolean add(double value) {
        if (lastSaveMs <= 0) lastSaveMs = createLastSaveMs();
        if (value < 0.0) return false;
        total += value;
        count++;
        return System.currentTimeMillis() - lastSaveMs >= savePeriodMs;
    }

    public double getAverageAndReset() {
        lastSaveMs = createLastSaveMs();
        double average = total / count;
        total = 0.0;
        count = 0;
        return average;
    }

    private long createLastSaveMs() {
        long time = System.currentTimeMillis();
        return time - (time % savePeriodMs);
    }
}