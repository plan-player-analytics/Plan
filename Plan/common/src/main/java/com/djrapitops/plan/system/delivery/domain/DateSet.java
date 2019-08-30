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
package com.djrapitops.plan.system.delivery.domain;

import java.util.TreeSet;

/**
 * Basic TreeSet with Epoch ms as values.
 *
 * @author Rsl1122
 */
public class DateSet extends TreeSet<Long> {

    public boolean hasValuesBetween(long after, long before) {
        return countBetween(after, before) > 0;
    }

    public int countBetween(long after, long before) {
        return subSet(after, before).size();
    }

}