/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.TestInit;

/**
 *
 * @author Risto
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class SettingsTest {

    public SettingsTest() {
    }

    @Before
    public void setUp() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        Plan plan = t.getPlanMock();
        PowerMock.mockStatic(JavaPlugin.class);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
//        PowerMock.verify(JavaPlugin.class);
    }

    @Test
    public void testIsTrue() {
        assertTrue("Webserver supposed to be enabled by default", Settings.WEBSERVER_ENABLED.isTrue());
    }

    @Test
    public void testToString() {
        assertEquals("sqlite",Settings.DB_TYPE.toString());
    }

    @Test
    public void testGetNumber() {
        assertEquals(8804,Settings.WEBSERVER_PORT.getNumber());
    }

    @Test
    public void testGetPath() {
        assertEquals("Settings.WebServer.Enabled", Settings.WEBSERVER_ENABLED.getPath());
    }

}
