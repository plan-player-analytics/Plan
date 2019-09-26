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
package com.djrapitops.plan.delivery.webserver.cache;

import com.djrapitops.plan.delivery.webserver.response.data.JSONResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests JSONCache invalidation.
 *
 * @author Rsl1122
 */
@RunWith(JUnitPlatform.class)
class JSONCacheTest {

    private static final String CACHED = "Cached";
    private static final DataID TEST_ID = DataID.SESSIONS;
    private static final UUID TEST_UUID = UUID.randomUUID();

    @BeforeEach
    void cleanCache() {
        JSONCache.invalidateAll();
    }

    @Test
    void cachedByDataIDName() {
        JSONCache.getOrCache(TEST_ID, () -> new JSONResponse(CACHED));
        assertContains();
    }

    private void assertContains() {
        List<String> cached = JSONCache.getCachedIDs();
        assertTrue(cached.contains(TEST_ID.name()));
    }

    private void assertNotContains() {
        List<String> cached = JSONCache.getCachedIDs();
        assertFalse(cached.contains(TEST_ID.name()));
    }

    @Test
    void invalidatedByExactDataID() {
        cachedByDataIDName();
        JSONCache.invalidate(TEST_ID);
        assertNotContains();
    }

    @Test
    void allInvalidated() {
        cachedByDataIDName();
        JSONCache.invalidateAll();
        assertNotContains();
    }

    @Test
    void cachedByServerUUID() {
        JSONCache.getOrCache(TEST_ID, TEST_UUID, () -> new JSONResponse(CACHED));
        assertContainsUUID();
    }

    private void assertContainsUUID() {
        List<String> cached = JSONCache.getCachedIDs();
        assertTrue(cached.contains(TEST_ID.of(TEST_UUID)));
    }

    private void assertNotContainsUUID() {
        List<String> cached = JSONCache.getCachedIDs();
        assertFalse(cached.contains(TEST_ID.of(TEST_UUID)));
    }

    @Test
    void invalidateByServerUUID() {
        cachedByServerUUID();
        JSONCache.invalidate(TEST_ID, TEST_UUID);
        assertNotContainsUUID();
    }

    @Test
    void invalidateMatchingByID() {
        cachedByDataIDName();
        cachedByServerUUID();
        JSONCache.invalidateMatching(TEST_ID);
        assertNotContains();
        assertNotContainsUUID();
    }

    @Test
    void invalidateMatchingByIDVarargs() {
        cachedByDataIDName();
        cachedByServerUUID();
        JSONCache.invalidateMatching(TEST_ID, TEST_ID);
        assertNotContains();
        assertNotContainsUUID();
    }
}