/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.DeathInfo;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.MockUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
public class DeathInfoTest {

    /**
     *
     */
    public DeathInfoTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     *
     */
    @Test
    public void testProcess() {
        UserData data = MockUtils.mockUser();
        DeathInfo i = new DeathInfo(data.getUuid());
        assertTrue(i.process(data));
        assertEquals(1, data.getDeaths());
    }

    /**
     *
     */
    @Test
    public void testProcessWrongUUID() {
        UserData data = MockUtils.mockUser();
        DeathInfo i = new DeathInfo(null);
        assertTrue(!i.process(data));
        assertEquals(0, data.getDeaths());
    }

}
