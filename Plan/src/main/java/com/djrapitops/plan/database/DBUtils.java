/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Rsl1122
 * @since 3.4.3
 */
public class DBUtils {

    public static <T> List<List<Container<T>>> splitIntoBatches(Map<Integer, List<T>> objects) {
        int batchSize = 2048;
        List<List<Container<T>>> wrappedBatches = new ArrayList<>();

        int i = 0;
        int j = 0;

        for (Entry<Integer, List<T>> entry : objects.entrySet()) {
            for (T object : entry.getValue()) {
                if (wrappedBatches.size() - 1 <= j) {
                    wrappedBatches.add(new ArrayList<>());
                }
                wrappedBatches.get(j).add(new Container<>(object, entry.getKey()));
                i++;                
                if (i % batchSize == 0) {
                    j++;
                }
            }
        }
        return wrappedBatches;
    }
}
