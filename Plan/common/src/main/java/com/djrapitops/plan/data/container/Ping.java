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
package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.store.objects.DateObj;

import java.util.UUID;

public class Ping extends DateObj<Double> {

    private final UUID serverUUID;
    private final double average;
    private final int min;
    private final int max;

    public Ping(long date, UUID serverUUID, int min, int max, double average) {
        super(date, average);
        this.serverUUID = serverUUID;
        this.average = average;
        this.min = min;
        this.max = max;
    }

    public UUID getServerUUID() {
        return serverUUID;
    }

    public double getAverage() {
        return average;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
