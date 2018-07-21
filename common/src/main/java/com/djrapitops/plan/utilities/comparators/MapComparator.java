package com.djrapitops.plan.utilities.comparators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Rsl1122
 */
public class MapComparator {

    /**
     * Constructor used to hide the public constructor
     */
    private MapComparator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Sorts a Map of String, Integer by the Values of the Map.
     *
     * @param map Map to sort
     * @return List with String Array, where first value is the value and second
     * is the key.
     */
    public static List<String[]> sortByValue(Map<String, Integer> map) {
        List<String[]> sortedList = new ArrayList<>();
        map.keySet().forEach(key -> sortedList.add(new String[]{String.valueOf(map.get(key)), key}));
        sortedList.sort(Comparator.comparingInt(strings -> Integer.parseInt(strings[0])));
        return sortedList;
    }

}
