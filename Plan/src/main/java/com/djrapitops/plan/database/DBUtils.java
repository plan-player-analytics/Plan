/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class containing static utility methods used by the Database classes.
 *
 * @author Rsl1122
 * @since 3.4.3
 */
public class DBUtils {

    private static final int BATCH_SIZE = 10192;

    /**
     * Constructor used to hide the public constructor
     */
    private DBUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Splits a collection of objects into lists with the size defined by
     * BATCH_SIZE.
     *
     * @param <T>     Object type
     * @param objects Collection of the objects
     * @return Lists with max size of BATCH_SIZE
     */
    public static <T> List<List<T>> splitIntoBatches(Collection<T> objects) {
        List<List<T>> batches = new ArrayList<>();

        int i = 0;
        int j = 0;

        for (T obj : objects) {
            if (batches.size() <= j) {
                batches.add(new ArrayList<>());
            }
            batches.get(j).add(obj);
            i++;
            if (i % BATCH_SIZE == 0) {
                j++;
            }
        }
        return batches;
    }

    /**
     * @param <T>     Object type
     * @param objects Collection of the objects
     * @return Lists with max size of BATCH_SIZE
     */
    public static <T> List<List<Container<T>>> splitIntoBatchesId(Map<Integer, List<T>> objects) {
        List<List<Container<T>>> wrappedBatches = new ArrayList<>();

        int i = 0;
        int j = 0;

        for (Entry<Integer, List<T>> entry : objects.entrySet()) {
            for (T object : entry.getValue()) {
                if (wrappedBatches.size() <= j) {
                    wrappedBatches.add(new ArrayList<>());
                }

                wrappedBatches.get(j).add(new Container<>(object, entry.getKey()));
                i++;
                if (i % BATCH_SIZE == 0) {
                    j++;
                }
            }
        }
        return wrappedBatches;
    }

    /**
     * @param <T>     Object type
     * @param objects Collection of the objects
     * @return Lists with max size of BATCH_SIZE
     */
    public static <T> List<List<Container<T>>> splitIntoBatchesWithID(Map<Integer, T> objects) {
        List<List<Container<T>>> wrappedBatches = new ArrayList<>();

        int i = 0;
        int j = 0;

        for (Entry<Integer, T> entry : objects.entrySet()) {
            T object = entry.getValue();
            if (wrappedBatches.size() <= j) {
                wrappedBatches.add(new ArrayList<>());
            }

            wrappedBatches.get(j).add(new Container<>(object, entry.getKey()));
            i++;
            if (i % BATCH_SIZE == 0) {
                j++;
            }
        }
        return wrappedBatches;
    }
}
