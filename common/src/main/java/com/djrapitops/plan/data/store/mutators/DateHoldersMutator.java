package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class DateHoldersMutator<T extends DateHolder> {

    private final List<T> dateHolders;

    public DateHoldersMutator(List<T> dateHolders) {
        this.dateHolders = dateHolders;
    }

    public TreeMap<Long, List<T>> groupByStartOfMinute() {
        return groupByStartOfSections(TimeAmount.MINUTE.ms());
    }

    private TreeMap<Long, List<T>> groupByStartOfSections(long sectionLength) {
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

    public TreeMap<Long, List<T>> groupByStartOfDay() {
        long twentyFourHours = 24L * TimeAmount.HOUR.ms();
        TreeMap<Long, List<T>> map = groupByStartOfSections(twentyFourHours);

        // Empty map firstKey attempt causes NPE if not checked.
        if (!map.isEmpty()) {
            // Add missing in-between dates
            long start = map.firstKey();
            long now = System.currentTimeMillis();
            long end = now - (now % twentyFourHours);
            for (long date = map.firstKey(); date < end; date += twentyFourHours) {
                map.putIfAbsent(date, new ArrayList<>());
            }
        }
        return map;
    }

}
