/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.data;

import com.djrapitops.plan.data.container.PlayerKill;
import org.junit.Test;
import utilities.RandomData;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for {@link PlayerKill}.
 *
 * @author Rsl1122
 */
public class PlayerKillTest {

    private String weapon = RandomData.randomString(10);
    private UUID testUUID = UUID.randomUUID();
    private PlayerKill underTest = new PlayerKill(testUUID, weapon, 100L);

    @Test
    public void victimUUIDIsReturned() {
        assertEquals(testUUID, underTest.getVictim());
    }

    @Test
    public void dateIsReturned() {
        assertEquals(100L, underTest.getDate());
    }

    @Test
    public void weaponIsReturned() {
        assertEquals(weapon, underTest.getWeapon());
    }

    @Test
    public void noVictimFound() {
        assertFalse(underTest.getVictimName().isPresent());
    }

    @Test
    public void victimFound() {
        String expectedName = "Test Victim";
        PlayerKill underTest = new PlayerKill(testUUID, weapon, 100L, expectedName);
        assertEquals("Test Victim", underTest.getVictimName().orElse("Unknown"));
    }
}
