package main.java.com.djrapitops.plan.utilities.comparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MapComparator {

    public static List<String[]> sortByValue(HashMap<String, Integer> hashMap) {
        List<String[]> sortedList = new ArrayList<>();
        hashMap.keySet().stream().forEach((key) -> {
            sortedList.add(new String[]{""+hashMap.get(key), key});
        });
        Collections.sort(sortedList, (String[] strings, String[] otherStrings) -> strings[0].compareTo(otherStrings[0]));
        return sortedList;
    }

    
}
