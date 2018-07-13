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

    public TreeMap<Long, List<T>> groupByStartOfDay() {
        TreeMap<Long, List<T>> map = new TreeMap<>();

        if (dateHolders.isEmpty()) {
            return map;
        }

        long twentyFourHours = 24L * TimeAmount.HOUR.ms();
        for (T holder : dateHolders) {
            long date = holder.getDate();
            long startOfDate = date - (date % twentyFourHours);

            List<T> list = map.getOrDefault(startOfDate, new ArrayList<>());
            list.add(holder);
            map.put(startOfDate, list);
        }

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
