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

import com.djrapitops.plan.delivery.domain.DateHolder;
import com.djrapitops.plan.utilities.java.Lists;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DateHoldersMutator<T extends DateHolder> {

    private final List<T> dateHolders;

    public DateHoldersMutator(List<T> dateHolders) {
        this.dateHolders = dateHolders;
    }

    public SortedMap<Long, List<T>> groupByStartOfMinute() {
        TreeMap<Long, List<T>> map = new TreeMap<>();

        if (dateHolders.isEmpty()) {
            return map;
        }

        long sectionLength = TimeUnit.MINUTES.toMillis(1L);
        for (T holder : dateHolders) {
            long date = holder.getDate();
            long startOfSection = date - (date % sectionLength);

            List<T> list = map.computeIfAbsent(startOfSection, Lists::create);
            list.add(holder);
        }
        return map;
    }

    public SortedMap<Long, List<T>> groupByStartOfDay(TimeZone timeZone) {
        long twentyFourHours = TimeUnit.DAYS.toMillis(1L);
        TreeMap<Long, List<T>> byStart = new TreeMap<>();

        if (dateHolders.isEmpty()) {
            return byStart;
        }

        for (T holder : dateHolders) {
            long date = holder.getDate();
            long dateWithOffset = date + timeZone.getOffset(date);
            long startOfSection = dateWithOffset - (dateWithOffset % twentyFourHours);

            List<T> grouped = byStart.computeIfAbsent(startOfSection, Lists::create);
            grouped.add(holder);
        }

        // Empty map firstKey attempt causes NPE if not checked.
        if (!byStart.isEmpty()) {
            // Add missing in-between dates
            long start = byStart.firstKey();
            long now = System.currentTimeMillis();
            long end = now - (now % twentyFourHours);
            for (long date = start; date < end; date += twentyFourHours) {
                byStart.putIfAbsent(date, new ArrayList<>());
            }
        }
        return byStart;
    }

}
