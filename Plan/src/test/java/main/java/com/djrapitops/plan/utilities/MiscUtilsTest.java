/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities;

import java.util.Set;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
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
public class MiscUtilsTest {

    public MiscUtilsTest() {
    }
    
    @Before
    public void setUp() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        Plan plan = t.getPlanMock();
        PowerMock.mockStatic(JavaPlugin.class);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
//        PowerMock.verify(JavaPlugin.class);
    }
    
    @Test
    public void testCheckVersion() {
        String versionG = "2.10.9";
        String result = MiscUtils.checkVersion("2.0.0", versionG);
        String exp = Phrase.VERSION_NEW_AVAILABLE.parse(versionG);
        assertEquals(exp, result);
    }
    
    @Test
    public void testCheckVersion2() {
        String result = MiscUtils.checkVersion("3.0.0", "2.10.9");
        String exp = Phrase.VERSION_LATEST+"";
        assertEquals(exp, result);
    }
    
    @Test
    public void testCheckVersion3() {
        String result = MiscUtils.checkVersion("2.11.0", "2.10.9");
        String exp = Phrase.VERSION_LATEST+"";
        assertEquals(exp, result);
    }
    
    @Test
    public void testCheckVersion4() {
        String result = MiscUtils.checkVersion("2.11.0", "2.11.0");
        String exp = Phrase.VERSION_LATEST+"";
        assertEquals(exp, result);
    }

    @Ignore
    @Test
    public void testGetPlayerDisplayname() {
        System.out.println("getPlayerDisplayname");
        String[] args = null;
        CommandSender sender = null;
        String expResult = "";
        String result = MiscUtils.getPlayerDisplayname(args, sender);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Ignore
    @Test
    public void testGetMatchingDisplaynames() {
        System.out.println("getMatchingDisplaynames");
        String search = "";
        Set<OfflinePlayer> expResult = null;
        Set<OfflinePlayer> result = MiscUtils.getMatchingDisplaynames(search);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
