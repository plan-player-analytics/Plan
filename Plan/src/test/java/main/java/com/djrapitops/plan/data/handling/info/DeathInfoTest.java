/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.DeathInfo;
import org.bukkit.plugin.java.JavaPlugin;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

/**
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
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
    public void setUp() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
    }

    /**
     *
     */
    @Test
    public void testProcess() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        DeathInfo i = new DeathInfo(data.getUuid());
        assertTrue(i.process(data));
        assertEquals(1, data.getDeaths());
    }

    /**
     *
     */
    @Test
    public void testProcessWrongUUID() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        DeathInfo i = new DeathInfo(null);
        assertTrue(!i.process(data));
        assertEquals(0, data.getDeaths());
    }

}
