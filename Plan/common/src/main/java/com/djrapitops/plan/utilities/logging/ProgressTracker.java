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
package com.djrapitops.plan.utilities.logging;

/**
 * @author AuroraLS3
 */
public class ProgressTracker {

    private int total;

    private int previousPercentage = 0;
    private int count;

    public ProgressTracker(int total) {
        this.total = total;
    }

    public void add(int amount) {
        count += amount;
    }

    public int getPercentage() {
        return total != 0 ? (int) Math.ceil(count * 100.0 / total) : 100;
    }

    public boolean shouldShowPercentage() {
        int percentage = getPercentage();
        return previousPercentage > percentage || percentage == 100 || percentage - previousPercentage >= 5;
    }

    public void percentageShown() {
        previousPercentage = getPercentage();
    }

    public int getTotal() {
        return total;
    }

    public int getCount() {
        return count;
    }

    public void reset(int total) {
        this.count = 0;
        this.previousPercentage = 0;
        this.total = total;
    }

    public boolean isDone() {
        return count >= total;
    }
}
