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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

/**
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class, Bukkit.class})
public class MiscUtilsTest {

    /**
     *
     */
    public MiscUtilsTest() {
    }

    /**
     *
     */
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

        PowerMock.mockStatic(Bukkit.class);
        OfflinePlayer op = MockUtils.mockPlayer();
        OfflinePlayer op2 = MockUtils.mockPlayer2();
        OfflinePlayer[] ops = new OfflinePlayer[]{op, op2};
        EasyMock.expect(Bukkit.getOfflinePlayers()).andReturn(ops);
        PowerMock.replay(Bukkit.class);
    }

    /**
     *
     */
    @Test
    public void testCheckVersion() {
        String versionG = "2.10.9";
        String result = MiscUtils.checkVersion("2.0.0", versionG);
        String exp = Phrase.VERSION_NEW_AVAILABLE.parse(versionG);
        assertEquals(exp, result);
    }

    /**
     *
     */
    @Test
    public void testCheckVersion2() {
        String result = MiscUtils.checkVersion("3.0.0", "2.10.9");
        String exp = Phrase.VERSION_LATEST + "";
        assertEquals(exp, result);
    }

    /**
     *
     */
    @Test
    public void testCheckVersion3() {
        String result = MiscUtils.checkVersion("2.11.0", "2.10.9");
        String exp = Phrase.VERSION_LATEST + "";
        assertEquals(exp, result);
    }

    /**
     *
     */
    @Test
    public void testCheckVersion4() {
        String result = MiscUtils.checkVersion("2.11.0", "2.11.0");
        String exp = Phrase.VERSION_LATEST + "";
        assertEquals(exp, result);
    }

    /**
     *
     */
    @Test
    public void testGetPlayerDisplaynameArgsPerm() {
        String[] args = new String[]{"Rsl1122", "Test"};
        CommandSender sender = MockUtils.mockPlayer();
        String expResult = "Rsl1122";
        String result = MiscUtils.getPlayerName(args, sender);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testGetPlayerDisplaynameArgsNoPerm() {
        String[] args = new String[]{"Rsl1122", "Test"};
        CommandSender sender = MockUtils.mockPlayer();
        String expResult = "Rsl1122";
        String result = MiscUtils.getPlayerName(args, sender);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testGetPlayerDisplaynameNoArgsPerm() {
        String[] args = new String[]{};
        CommandSender sender = MockUtils.mockPlayer();
        String expResult = "TestName";
        String result = MiscUtils.getPlayerName(args, sender);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testGetPlayerDisplaynameNoArgsNoPerm() {
        String[] args = new String[]{};
        CommandSender sender = MockUtils.mockPlayer2();
        String expResult = "TestName2";
        String result = MiscUtils.getPlayerName(args, sender);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testGetPlayerDisplaynameOwnNameNoPerm() {
        String[] args = new String[]{"testname2"};
        CommandSender sender = MockUtils.mockPlayer2();
        String expResult = "TestName2";
        String result = MiscUtils.getPlayerName(args, sender);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testGetPlayerDisplaynameConsole() {
        String[] args = new String[]{"TestConsoleSender"};
        CommandSender sender = MockUtils.mockConsoleSender();
        String expResult = "TestConsoleSender";
        String result = MiscUtils.getPlayerName(args, sender);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testGetMatchingDisplaynames() {
        String search = "testname";
        OfflinePlayer exp1 = MockUtils.mockPlayer();
        OfflinePlayer exp2 = MockUtils.mockPlayer2();
        Set<OfflinePlayer> result = MiscUtils.getMatchingDisplaynames(search);
        assertEquals(2, result.size());
        for (OfflinePlayer r : result) {
            boolean equalToExp1 = r.getName().equals(exp1.getName());
            boolean equalToExp2 = r.getName().equals(exp2.getName());
            if (!(equalToExp1 || equalToExp2)) {
                fail("Unknown result!: "+r.getName());
            }
        }
    }

    /**
     *
     */
    @Test
    public void testGetMatchingDisplaynames2() {
        String search = "2";
        OfflinePlayer exp2 = MockUtils.mockPlayer2();
        Set<OfflinePlayer> result = MiscUtils.getMatchingDisplaynames(search);
        assertEquals(1, result.size());
        for (OfflinePlayer r : result) {
            if (!r.getName().equals(exp2.getName())) {
                fail("Unknown result!: "+r.getName());
            }
        }
    }
}
