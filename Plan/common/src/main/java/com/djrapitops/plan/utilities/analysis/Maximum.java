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

/**
 * Calculates maximum from given values.
 *
 * @author AuroraLS3
 */
public interface Maximum {

    class ForInteger {
        private int max;
        private final int startingValue;

        public ForInteger() {
            this(Integer.MIN_VALUE);
        }

        public ForInteger(int startingValue) {
            this.startingValue = startingValue;
            this.max = startingValue;
        }

        public void add(int value) {
            if (value > max) max = value;
        }

        public int getMaxAndReset() {
            int result = max;
            max = startingValue;
            return result;
        }
    }

}