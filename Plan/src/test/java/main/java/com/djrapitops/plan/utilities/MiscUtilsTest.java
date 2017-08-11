/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.bukkit.BukkitCMDSender;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
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
    public void setUp() throws Exception {
    }

    /**
     *
     */
    @Test
    public void testGetPlayerDisplaynameArgsPerm() {
        String[] args = new String[]{"Rsl1122", "Test"};
        ISender sender = new BukkitCMDSender(MockUtils.mockPlayer());
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
        ISender sender = new BukkitCMDSender(MockUtils.mockPlayer());
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
        ISender sender = new BukkitCMDSender(MockUtils.mockPlayer());
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
        ISender sender = new BukkitCMDSender(MockUtils.mockPlayer2());
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
        ISender sender = new BukkitCMDSender(MockUtils.mockPlayer2());
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
        ISender sender = new BukkitCMDSender(MockUtils.mockConsoleSender());
        String expResult = "TestConsoleSender";
        String result = MiscUtils.getPlayerName(args, sender);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    @Ignore("DB mock")
    public void testGetMatchingDisplaynames() throws Exception {
        TestInit.init();
        String search = "testname";
        String exp1 = "TestName";
        String exp2 = "TestName2";
        List<String> result = MiscUtils.getMatchingPlayerNames(search);
        assertEquals(2, result.size());
        assertEquals(exp1, result.get(0));
        assertEquals(exp2, result.get(1));
    }

    /**
     *
     */
    @Test
    @Ignore("DB mock")
    public void testGetMatchingDisplaynames2() throws Exception {
        TestInit.init();
        String search = "2";
        String exp2 = "TestName2";
        List<String> result = MiscUtils.getMatchingPlayerNames(search);
        assertEquals(1, result.size());
        assertEquals(exp2, result.get(0));
    }
}
