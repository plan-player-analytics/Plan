package main.java.com.djrapitops.plan.utilities.comparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
    public static List<String[]> sortByValue(HashMap<String, Integer> hashMap) {
        List<String[]> sortedList = new ArrayList<>();
        hashMap.keySet().stream().forEach((key) -> {
            sortedList.add(new String[]{"" + hashMap.get(key), key});
        });
        Collections.sort(sortedList, (String[] strings, String[] otherStrings) -> Integer.parseInt(strings[0]) - (Integer.parseInt(otherStrings[0])));
        return sortedList;
    }

    public static List<String[]> sortByValueLong(HashMap<String, Long> hashMap) {
        List<String[]> sortedList = new ArrayList<>();
        hashMap.keySet().stream().forEach((key) -> {
            sortedList.add(new String[]{"" + hashMap.get(key), key});
        });
        Collections.sort(sortedList, (String[] strings, String[] otherStrings) -> (int) (Long.parseLong(strings[0]) - (Long.parseLong(otherStrings[0]))));
        return sortedList;
    }

}
