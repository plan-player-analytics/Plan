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
package com.djrapitops.plan.delivery.domain.datatransfer.preferences;

import java.util.Objects;

/**
 * @author AuroraLS3
 */
public class GraphThresholds {

    private double highThreshold;
    private double mediumThreshold;

    public GraphThresholds(double highThreshold, double mediumThreshold) {
        this.highThreshold = highThreshold;
        this.mediumThreshold = mediumThreshold;
    }

    public double getHighThreshold() {
        return highThreshold;
    }

    public void setHighThreshold(int highThreshold) {
        this.highThreshold = highThreshold;
    }

    public double getMediumThreshold() {
        return mediumThreshold;
    }

    public void setMediumThreshold(int mediumThreshold) {
        this.mediumThreshold = mediumThreshold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphThresholds that = (GraphThresholds) o;
        return getHighThreshold() == that.getHighThreshold() && getMediumThreshold() == that.getMediumThreshold();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHighThreshold(), getMediumThreshold());
    }

    @Override
    public String toString() {
        return "GraphThresholds{" +
                "highThreshold=" + highThreshold +
                ", mediumThreshold=" + mediumThreshold +
                '}';
    }
}
