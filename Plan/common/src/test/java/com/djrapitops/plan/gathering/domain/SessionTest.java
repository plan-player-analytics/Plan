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

import com.djrapitops.plan.identification.ServerUUID;
import org.junit.jupiter.api.Test;
import utilities.TestConstants;
import utilities.TestData;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ActiveSession} and {@link FinishedSession}.
 *
 * @author AuroraLS3
 */
class SessionTest {

    private final ServerUUID serverUUID = TestConstants.SERVER_UUID;

    @Test
    void killsAreAdded() {
        ActiveSession session = new ActiveSession(null, serverUUID, System.currentTimeMillis(), "", "");

        Optional<List<PlayerKill>> beforeOptional = session.getExtraData().get(PlayerKills.class).map(PlayerKills::asList);
        assertTrue(beforeOptional.isPresent());
        List<PlayerKill> before = beforeOptional.get();
        assertTrue(before.isEmpty());

        session.addPlayerKill(TestData.getPlayerKill(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_TWO_UUID, serverUUID, "Weapon", System.currentTimeMillis()));

        Optional<List<PlayerKill>> afterOptional = session.getExtraData().get(PlayerKills.class).map(PlayerKills::asList);
        assertTrue(afterOptional.isPresent());
        List<PlayerKill> after = afterOptional.get();

        assertFalse(after.isEmpty());
        assertEquals(before, after);
    }

    @Test
    void killsAreAdded2() {
        ActiveSession session = new ActiveSession(null, serverUUID, System.currentTimeMillis(), "", "");

        session.addPlayerKill(TestData.getPlayerKill(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_TWO_UUID, serverUUID, "Weapon", System.currentTimeMillis()));

        Optional<List<PlayerKill>> afterOptional = session.getExtraData().get(PlayerKills.class).map(PlayerKills::asList);
        assertTrue(afterOptional.isPresent());
        List<PlayerKill> after = afterOptional.get();

        assertFalse(after.isEmpty());
    }

    @Test
    void worldTimesWorks() {
        long time = System.currentTimeMillis();
        ActiveSession session = new ActiveSession(null, serverUUID, time, "One", "Survival");
        session.changeState("Two", "Three", time + 5L);

        Optional<WorldTimes> optional = session.getExtraData().get(WorldTimes.class);
        assertTrue(optional.isPresent());
        WorldTimes worldTimes = optional.get();

        assertEquals(5L, worldTimes.getGMTimes("One").getTotal());
    }
}