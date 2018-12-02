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
package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.store.objects.DateHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class DateHoldersMutator<T extends DateHolder> {

    private final List<T> dateHolders;

    public DateHoldersMutator(List<T> dateHolders) {
        this.dateHolders = dateHolders;
    }

    public SortedMap<Long, List<T>> groupByStartOfMinute() {
        return groupByStartOfSections(TimeUnit.MINUTES.toMillis(1L));
    }

    private SortedMap<Long, List<T>> groupByStartOfSections(long sectionLength) {
        TreeMap<Long, List<T>> map = new TreeMap<>();

        if (dateHolders.isEmpty()) {
            return map;
        }

        for (T holder : dateHolders) {
            long date = holder.getDate();
            long startOfMinute = date - (date % sectionLength);

            List<T> list = map.getOrDefault(startOfMinute, new ArrayList<>());
            list.add(holder);
            map.put(startOfMinute, list);
        }
        return map;
    }

    public SortedMap<Long, List<T>> groupByStartOfDay() {
        long twentyFourHours = TimeUnit.DAYS.toMillis(1L);
        SortedMap<Long, List<T>> map = groupByStartOfSections(twentyFourHours);

        // Empty map firstKey attempt causes NPE if not checked.
        if (!map.isEmpty()) {
            // Add missing in-between dates
            long start = map.firstKey();
            long now = System.currentTimeMillis();
            long end = now - (now % twentyFourHours);
            for (long date = start; date < end; date += twentyFourHours) {
                map.putIfAbsent(date, new ArrayList<>());
            }
        }
        return map;
    }

}
