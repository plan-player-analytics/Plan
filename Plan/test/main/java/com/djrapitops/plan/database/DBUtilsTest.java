/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.database;

import org.junit.Test;
import test.java.utils.RandomData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author ristolah
 */
public class DBUtilsTest {

    @Test
    public void testSplitIntoBatches() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 21336; i++) {
            list.add(i);
        }

        List<List<Integer>> result = DBUtils.splitIntoBatches(list);

        assertEquals(3, result.size());
        assertEquals(10192, result.get(0).size());
        assertEquals(10192, result.get(1).size());
        assertEquals(952, result.get(2).size());
    }

    @Test
    public void testSplitIntoBatchesSingleBatch() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10192; i++) {
            list.add(i);
        }

        List<List<Integer>> result = DBUtils.splitIntoBatches(list);

        assertEquals(1, result.size());
        assertEquals(10192, result.get(0).size());
    }

    @Test
    public void testSplitIntoBatchesId() {
        Map<Integer, List<Integer>> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 2133; j++) {
                map.computeIfAbsent(i, k -> new ArrayList<>());
                map.get(i).add(j);
            }
        }

        List<List<Container<Integer>>> result = DBUtils.splitIntoBatchesId(map);

        assertEquals(3, result.size());
        assertEquals(10192, result.get(0).size());
        assertEquals(10192, result.get(1).size());
        assertEquals(946, result.get(2).size());
    }

    @Test
    public void testContainers() {
        Object object = new Object();
        int id = RandomData.randomInt(1, 100);

        Container<Object> container = new Container<>(object, id);

        assertEquals(id, container.getId());
        assertEquals(object, container.getObject());
    }

}
