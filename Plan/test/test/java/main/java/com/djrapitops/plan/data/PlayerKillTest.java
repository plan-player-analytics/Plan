/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.data.PlayerKill;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
public class PlayerKillTest {

    private PlayerKill test;
    private UUID testUUID;

    /**
     *
     */
    public PlayerKillTest() {
    }

    @Before
    public void setUp() throws Exception {
        testUUID = UUID.fromString("71cfb6f0-c3ef-4954-8abe-13fa07afc340");
        test = new PlayerKill(testUUID, "TestWeapon", 100L);
    }

    /**
     *
     */
    @Test
    public void testGetVictim() {
        assertEquals(test.getVictim(), testUUID);
    }

    /**
     *
     */
    @Test
    public void testGetDate() {
        assertEquals(test.getTime(), 100L);
    }

    /**
     *
     */
    @Test
    public void testGetWeapon() {
        assertEquals(test.getWeapon(), "TestWeapon");
    }
}
