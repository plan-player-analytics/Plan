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
package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;

import java.util.List;
import java.util.Optional;

/**
 * @author AuroraLS3
 */
public class DateObjMutator<T> {

    private final List<DateObj<T>> dateObjects;

    public DateObjMutator(List<DateObj<T>> dateObjects) {this.dateObjects = dateObjects;}

    public Optional<DateObj<T>> mostRecent() {
        if (dateObjects.isEmpty()) return Optional.empty();
        dateObjects.sort(new DateHolderRecentComparator());
        return Optional.of(dateObjects.get(0));
    }
}
