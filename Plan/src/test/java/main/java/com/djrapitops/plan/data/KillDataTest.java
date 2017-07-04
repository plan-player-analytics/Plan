/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data;

import java.util.UUID;
import main.java.com.djrapitops.plan.data.KillData;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Rsl1122
 */
public class KillDataTest {

    /**
     *
     */
    public KillDataTest() {
    }

    /**
     *
     */
    @Test
    public void testGetVictim() {
        UUID uuid = UUID.fromString("71cfb6f0-c3ef-4954-8abe-13fa07afc340");
        KillData k = new KillData(uuid, 1, "TestWeapon", 100L);
        assertEquals(k.getVictim(), uuid);
    }

    /**
     *
     */
    @Test
    public void testGetDate() {
        UUID uuid = UUID.fromString("71cfb6f0-c3ef-4954-8abe-13fa07afc340");
        KillData k = new KillData(uuid, 1, "TestWeapon", 100L);
        assertEquals(k.getDate(), 100L);
    }

    /**
     *
     */
    @Test
    public void testGetWeapon() {
        UUID uuid = UUID.fromString("71cfb6f0-c3ef-4954-8abe-13fa07afc340");
        KillData k = new KillData(uuid, 1, "TestWeapon", 100L);
        assertEquals(k.getWeapon(), "TestWeapon");
    }

    /**
     *
     */
    @Test
    public void testGetVictimUserID() {
        UUID uuid = UUID.fromString("71cfb6f0-c3ef-4954-8abe-13fa07afc340");
        KillData k = new KillData(uuid, 1, "TestWeapon", 100L);
        assertEquals(k.getVictimUserID(), 1);
    }

}
