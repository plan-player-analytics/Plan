/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
