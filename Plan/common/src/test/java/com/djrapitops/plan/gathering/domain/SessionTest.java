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
package com.djrapitops.plan.gathering.domain;

import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link Session} {@link DataContainer}.
 *
 * @author Rsl1122
 */
class SessionTest {

    private final UUID serverUUID = TestConstants.SERVER_UUID;

    @Test
    void safeStartKeyConstructor() {
        for (int i = 0; i < 10000; i++) {
            long expected = RandomData.randomLong(0, System.currentTimeMillis());
            Session session = new Session(null, serverUUID, expected, null, null);

            // Should not throw
            assertEquals(expected, session.getUnsafe(SessionKeys.START));
        }
    }

    @Test
    void safeStartKeyDBConstructor() {
        for (int i = 0; i < 10000; i++) {
            long expected = RandomData.randomLong(0, System.currentTimeMillis());
            Session session = new Session(-1, null, null, expected, expected + 1, 0, 0, 0);

            // Should not throw
            assertEquals(expected, session.getUnsafe(SessionKeys.START));
        }
    }

    @Test
    void killsAreAdded() {
        Session session = new Session(null, serverUUID, System.currentTimeMillis(), "", "");

        Optional<List<PlayerKill>> beforeOptional = session.getValue(SessionKeys.PLAYER_KILLS);
        assertTrue(beforeOptional.isPresent());
        List<PlayerKill> before = beforeOptional.get();
        assertTrue(before.isEmpty());

        session.playerKilled(new PlayerKill(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_TWO_UUID, "Weapon", System.currentTimeMillis()));

        Optional<List<PlayerKill>> afterOptional = session.getValue(SessionKeys.PLAYER_KILLS);
        assertTrue(afterOptional.isPresent());
        List<PlayerKill> after = afterOptional.get();

        assertFalse(after.isEmpty());
        assertEquals(before, after);
    }

    @Test
    void killsAreAdded2() {
        Session session = new Session(null, serverUUID, System.currentTimeMillis(), "", "");

        session.playerKilled(new PlayerKill(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_TWO_UUID, "Weapon", System.currentTimeMillis()));

        Optional<List<PlayerKill>> afterOptional = session.getValue(SessionKeys.PLAYER_KILLS);
        assertTrue(afterOptional.isPresent());
        List<PlayerKill> after = afterOptional.get();

        assertFalse(after.isEmpty());
    }

    @Test
    void worldTimesWorks() {
        long time = System.currentTimeMillis();
        Session session = new Session(null, serverUUID, time, "One", "Survival");
        session.changeState("Two", "Three", time + 5L);

        Optional<WorldTimes> optional = session.getValue(SessionKeys.WORLD_TIMES);
        assertTrue(optional.isPresent());
        WorldTimes worldTimes = optional.get();

        assertEquals(5L, worldTimes.getGMTimes("One").getTotal());
    }
}