/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.KickInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class KickInfoTest {

    /**
     *
     */
    public KickInfoTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() throws Exception {
        TestInit.init();
    }

    /**
     *
     */
    @Test
    public void testProcess() {
        UserData data = MockUtils.mockUser();
        KickInfo i = new KickInfo(data.getUuid());
        i.process(data);
        assertEquals(1, data.getTimesKicked());
    }

}
