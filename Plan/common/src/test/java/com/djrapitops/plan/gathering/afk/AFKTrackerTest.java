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
package com.djrapitops.plan.gathering.afk;

import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import utilities.TestConstants;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AFKTrackerTest {

    private final UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
    private AFKTracker underTest;
    private long afkThreshold;

    @BeforeEach
    void setUpAfkTracker() {
        PlanConfig config = Mockito.mock(PlanConfig.class);
        afkThreshold = TimeUnit.MINUTES.toMillis(1L);
        when(config.get(TimeSettings.AFK_THRESHOLD)).thenReturn(afkThreshold);

        new SessionCache().cacheSession(playerUUID, new ActiveSession(playerUUID, null, 0, null, null));

        underTest = new AFKTracker(config);
    }

    @AfterEach
    void tearDown() {
        SessionCache.clear();
    }

    @Test
    void afkThresholdMatches() {
        assertEquals(afkThreshold, underTest.getAfkThreshold());
    }

    @Test
    void someoneIsAFKForAWhile() {
        underTest.performedAction(playerUUID, 0L);
        long afkTime = underTest.loggedOut(playerUUID, afkThreshold * 2);
        assertEquals(afkThreshold * 2, afkTime);
    }

    @Test
    void someoneIsAFKForAWhileButHasIgnorePermission() {
        underTest.hasIgnorePermission(playerUUID);
        underTest.performedAction(playerUUID, 0L);
        long afkTime = underTest.loggedOut(playerUUID, afkThreshold * 2);
        assertEquals(0L, afkTime);
    }

    @Test
    void someOneIsAfk() {
        underTest.performedAction(playerUUID, 0L);
        assertTrue(underTest.isAfk(playerUUID));
    }

    @Test
    void someOneIsNotEvenOnline() {
        assertFalse(underTest.isAfk(TestConstants.PLAYER_TWO_UUID));
    }

    @Test
    void someOneIsNotAfk() {
        underTest.performedAction(playerUUID, System.currentTimeMillis());
        assertFalse(underTest.isAfk(playerUUID));
    }

    @Test
    void someoneIsAFKForAwhileWithAfkCommand() {
        underTest.usedAfkCommand(playerUUID, 0L);
        long afkTime = underTest.loggedOut(playerUUID, afkThreshold * 2);
        assertEquals(afkThreshold * 2, afkTime);
    }

    @Test
    void someoneIsAFKForAwhileWithAfkCommandButHasIgnorePermission() {
        underTest.hasIgnorePermission(playerUUID);
        underTest.usedAfkCommand(playerUUID, 0L);
        long afkTime = underTest.loggedOut(playerUUID, afkThreshold * 2);
        assertEquals(0L, afkTime);
    }
}