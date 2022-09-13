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
package com.djrapitops.plan.gathering.cache;

import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utilities.TestConstants;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author AuroraLS3
 */
class JoinAddressCacheTest {

    private JoinAddressCache underTest;

    @BeforeEach
    void setUp() {
        underTest = new JoinAddressCache();
    }

    @Test
    void valueIsNotCached() {
        assertTrue(underTest.get(TestConstants.PLAYER_ONE_UUID).isEmpty());
    }

    @Test
    void valueIsCached() {
        underTest.put(TestConstants.PLAYER_ONE_UUID, "Test");

        Optional<JoinAddress> cached = underTest.get(TestConstants.PLAYER_ONE_UUID);
        assertTrue(cached.isPresent());
        assertEquals("Test", cached.get().getAddress());
    }

    @Test
    void valueIsCachedAsJoinAddress() {
        underTest.put(TestConstants.PLAYER_ONE_UUID, new JoinAddress("Test"));

        Optional<JoinAddress> cached = underTest.get(TestConstants.PLAYER_ONE_UUID);
        assertTrue(cached.isPresent());
        assertEquals("Test", cached.get().getAddress());
    }

    @Test
    void valueIsCachedAsString() {
        underTest.put(TestConstants.PLAYER_ONE_UUID, new JoinAddress("Test"));

        String cached = underTest.getNullableString(TestConstants.PLAYER_ONE_UUID);
        assertEquals("Test", cached);
    }

    @Test
    void valueIsNotCachedAsString() {
        String cached = underTest.getNullableString(TestConstants.PLAYER_ONE_UUID);
        assertNull(cached);
    }

    @Test
    void valueIsRemoved() {
        valueIsCached();
        underTest.remove(TestConstants.PLAYER_ONE_UUID);
        valueIsNotCached();
    }
}