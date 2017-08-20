/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.data.handling.info.InfoType;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
public class HandlingInfoTest {

    /**
     *
     */
    public HandlingInfoTest() {
    }

    /**
     *
     */
    @Test
    public void testGetUuid() {
        UUID uuid = UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db");
        HandlingInfo i = new HandlingInfo(uuid, InfoType.CHAT, 10L) {
            @Override
            public void process(UserData data) {

            }
        };
        assertEquals(uuid, i.getUuid());
    }

    /**
     *
     */
    @Test
    public void testGetType() {
        UUID uuid = UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db");
        HandlingInfo i = new HandlingInfo(uuid, InfoType.CHAT, 10L) {
            @Override
            public void process(UserData data) {

            }
        };
        assertEquals(InfoType.CHAT, i.getType());
    }

    /**
     *
     */
    @Test
    public void testGetTime() {
        UUID uuid = UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db");
        HandlingInfo i = new HandlingInfo(uuid, InfoType.CHAT, 10L) {
            @Override
            public void process(UserData data) {

            }
        };
        assertEquals(10L, i.getTime());
    }
}
