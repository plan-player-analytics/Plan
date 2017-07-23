package main.java.com.djrapitops.plan.utilities.comparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rsl1122
 */
public class MapComparator {

    /**
     * Sorts a HashMap of String, Integer by the Values of the HashMap.
     *
     * @param hashMap
     * @return List with String Array, where first value is the value and second
     * is the key.
     */
    public static List<String[]> sortByValue(Map<String, Integer> hashMap) {
        List<String[]> sortedList = new ArrayList<>();
        hashMap.keySet().stream().forEach((key) -> {
            sortedList.add(new String[]{"" + hashMap.get(key), key});
        });
        sortedList.sort((String[] strings, String[] otherStrings) -> Integer.parseInt(strings[0]) - (Integer.parseInt(otherStrings[0])));
        return sortedList;
    }

    /**
     *
     * @param hashMap
     * @return
     */
    public static List<String[]> sortByValueLong(Map<String, Long> hashMap) {
        List<String[]> sortedList = new ArrayList<>();
        hashMap.keySet().stream().forEach((key) -> {
            sortedList.add(new String[]{"" + hashMap.get(key), key});
        });
        sortedList.sort((String[] strings, String[] otherStrings) -> Long.valueOf(strings[0]).compareTo(Long.valueOf(otherStrings[0])));
        return sortedList;
    }

}
