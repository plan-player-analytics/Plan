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
package com.djrapitops.plan.extension.implementation.providers.gathering;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility object for managing conditions.
 *
 * @author AuroraLS3
 */
public class Conditions {

    private final Set<String> fulfilledConditions;

    public Conditions() {
        this.fulfilledConditions = new HashSet<>();
    }

    public boolean isNotFulfilled(String condition) {
        return !fulfilledConditions.contains(condition);
    }

    public void conditionFulfilled(String condition) {
        fulfilledConditions.add(condition);
    }
}