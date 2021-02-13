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
package com.djrapitops.plan.delivery.domain.containers;

import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.container.SupplierDataContainer;
import com.djrapitops.plan.delivery.domain.keys.Key;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link SupplierDataContainer} programming errors.
 *
 * @author AuroraLS3
 */
class SupplierDataContainerTest {

    private static final Key<String> TEST_KEY = new Key<>(String.class, "TEST_KEY");
    private static final Key<String> TEST_KEY_COPY = new Key<>(String.class, "TEST_KEY");

    @Test
    void safeUnsafeKeySupplierSameObject() {
        DataContainer container = new SupplierDataContainer();
        container.putSupplier(TEST_KEY, () -> "Success");

        assertEquals("Success", container.getUnsafe(TEST_KEY));
    }

    @Test
    void safeUnsafeKeySupplierDifferentObject() {
        DataContainer container = new SupplierDataContainer();
        container.putSupplier(TEST_KEY, () -> "Success");

        assertEquals("Success", container.getUnsafe(TEST_KEY_COPY));
    }

    @Test
    void safeUnsafeKeyRawSameObject() {
        DataContainer container = new SupplierDataContainer();
        container.putRawData(TEST_KEY, "Success");

        assertEquals("Success", container.getUnsafe(TEST_KEY));
    }

    @Test
    void safeUnsafeKeyRawDifferentObject() {
        DataContainer container = new SupplierDataContainer();
        container.putRawData(TEST_KEY, "Success");

        assertEquals("Success", container.getUnsafe(TEST_KEY_COPY));
    }

    @Test
    void safeUnsafeKeyRawNull() {
        DataContainer container = new SupplierDataContainer();
        container.putRawData(TEST_KEY, null);

        assertTrue(container.supports(TEST_KEY));
        assertNull(container.getUnsafe(TEST_KEY));
    }

    @Test
    void safeUnsafeKeyNullSupplier() {
        DataContainer container = new SupplierDataContainer();
        container.putSupplier(TEST_KEY, null);

        assertFalse(container.supports(TEST_KEY));
    }

    @Test
    void safeUnsafeKeySupplierNull() {
        DataContainer container = new SupplierDataContainer();
        container.putSupplier(TEST_KEY, () -> null);

        assertTrue(container.supports(TEST_KEY));
        assertNull(container.getUnsafe(TEST_KEY));
    }

    @Test
    void cachingSupplier() {
        DataContainer container = new SupplierDataContainer();
        String firstObj = "First";
        String secondObj = "Second";

        assertNotSame(firstObj, secondObj);

        container.putCachingSupplier(TEST_KEY, () -> firstObj);

        String found = container.getUnsafe(TEST_KEY);
        assertEquals(firstObj, found);
        assertSame(firstObj, found);
        assertNotSame(secondObj, found);

        String secondCall = container.getUnsafe(TEST_KEY);
        assertSame(found, secondCall);
    }

}