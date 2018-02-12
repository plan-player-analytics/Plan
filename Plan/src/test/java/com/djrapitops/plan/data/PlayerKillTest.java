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

/**
 * @author Rsl1122
 */
public class PlayerKillTest {

    private String randomString = RandomData.randomString(10);
    private UUID testUUID = UUID.randomUUID();
    private PlayerKill playerKill = new PlayerKill(testUUID, randomString, 100L);

    @Test
    public void testGetVictim() {
        assertEquals(playerKill.getVictim(), testUUID);
    }

    @Test
    public void testGetDate() {
        assertEquals(playerKill.getTime(), 100L);
    }

    @Test
    public void testGetWeapon() {
        assertEquals(playerKill.getWeapon(), randomString);
    }
}
