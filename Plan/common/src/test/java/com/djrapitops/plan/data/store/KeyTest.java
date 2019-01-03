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
package com.djrapitops.plan.data.store;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Equals test for Key objects.
 *
 * @author Rsl1122
 */
public class KeyTest {

    @Test
    public void twoInstancesAreEqual() {
        Key<Integer> testKey = new Key<>(Integer.class, "test");
        Key<Integer> testKey2 = new Key<>(Integer.class, "test");
        assertEquals(testKey, testKey2);
    }

    @Test
    public void twoComplexInstancesAreEqual() {
        Key<List<Integer>> testKey = new Key<>(new Type<List<Integer>>() {}, "test");
        Key<List<Integer>> testKey2 = new Key<>(new Type<List<Integer>>() {}, "test");
        assertEquals(testKey, testKey2);
    }

    @Test
    public void twoComplexInstancesAreNotEqual() {
        Key<List<Long>> testKey = new Key<>(new Type<List<Long>>() {}, "test");
        Key<List<Integer>> testKey2 = new Key<>(new Type<List<Integer>>() {}, "test");
        assertNotEquals(testKey, testKey2);
    }

    @Test
    public void twoComplexInstancesAreNotEqual2() {
        Key<ArrayList<Integer>> testKey = new Key<>(new Type<ArrayList<Integer>>() {}, "test");
        Key<List<Integer>> testKey2 = new Key<>(new Type<List<Integer>>() {}, "test");
        assertNotEquals(testKey, testKey2);
    }

    @Test
    public void twoInstancesAreNotEqual() {
        Key<Integer> testKey = new Key<>(Integer.class, "test");
        Key<List<Integer>> testKey2 = new Key<>(new Type<List<Integer>>() {}, "test");
        assertNotEquals(testKey, testKey2);
    }

}