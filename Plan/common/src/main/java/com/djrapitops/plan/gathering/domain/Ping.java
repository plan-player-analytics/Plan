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

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.identification.ServerUUID;

import java.util.Objects;

public class Ping extends DateObj<Double> {

    private final ServerUUID serverUUID;
    private final double average;
    private final int min;
    private final int max;

    public Ping(long date, ServerUUID serverUUID, int min, int max, double average) {
        super(date, average);
        this.serverUUID = serverUUID;
        this.average = average;
        this.min = min;
        this.max = max;
    }

    public ServerUUID getServerUUID() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ping)) return false;
        Ping ping = (Ping) o;
        return Double.compare(ping.average, average) == 0 &&
                min == ping.min &&
                max == ping.max &&
                Objects.equals(serverUUID, ping.serverUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverUUID, average, min, max);
    }

    @Override
    public String toString() {
        return "Ping{" +
                "serverUUID=" + serverUUID +
                ", average=" + average +
                ", min=" + min +
                ", max=" + max +
                '}';
    }
}
