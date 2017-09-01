/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.bukkit.BukkitCMDSender;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.database.tables.UsersTable;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfo;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.RandomData;
import test.java.utils.TestInit;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class, Bukkit.class})
public class MiscUtilsTest {

    private Plan plan;
    private SQLDB db;

    @Test
    public void testGetPlayerDisplayNameArgsPerm() {
        String[] args = new String[]{"Rsl1122", "Test"};
        ISender sender = new BukkitCMDSender(MockUtils.mockPlayer());

        String expResult = "Rsl1122";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    public void testGetPlayerDisplayNameArgsNoPerm() throws Exception {
        TestInit.init();

        String[] args = new String[]{"Rsl1122", "Test"};
        ISender sender = new BukkitCMDSender(MockUtils.mockPlayer2());

        String expResult = "";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    public void testGetPlayerDisplayNameNoArgsPerm() {
        String[] args = new String[]{};
        ISender sender = new BukkitCMDSender(MockUtils.mockPlayer());

        String expResult = "TestName";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    public void testGetPlayerDisplayNameNoArgsNoPerm() {
        String[] args = new String[]{};
        ISender sender = new BukkitCMDSender(MockUtils.mockPlayer2());

        String expResult = "TestName2";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    public void testGetPlayerDisplayNameOwnNameNoPerm() {
        String[] args = new String[]{"testname2"};
        ISender sender = new BukkitCMDSender(MockUtils.mockPlayer2());

        String expResult = "TestName2";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    public void testGetPlayerDisplayNameConsole() {
        String[] args = new String[]{"TestConsoleSender"};
        ISender sender = new BukkitCMDSender(MockUtils.mockConsoleSender());

        String expResult = "TestConsoleSender";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    public void testGetMatchingNames() throws Exception {
        setupDatabase();

        String exp1 = "TestName";
        String exp2 = "TestName2";

        UsersTable usersTable = db.getUsersTable();
        usersTable.registerUser(UUID.randomUUID(), 0L, exp1);
        usersTable.registerUser(UUID.randomUUID(), 0L, exp2);

        String search = "testname";

        List<String> result = MiscUtils.getMatchingPlayerNames(search);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(exp1, result.get(0));
        assertEquals(exp2, result.get(1));
    }

    @Test
    public void testGetMatchingNickNames() throws Exception {
        setupDatabase();

        UUID uuid = UUID.randomUUID();
        String userName = RandomData.randomString(10);
        db.getUsersTable().registerUser(uuid, 0L, userName);

        String nickname = "2" + RandomData.randomString(10);
        db.getNicknamesTable().saveUserName(uuid, nickname);

        String search = "2";

        List<String> result = MiscUtils.getMatchingPlayerNames(search);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userName, result.get(0));
    }

    private void setupDatabase() throws Exception {
        TestInit.init();

        TestInit t = TestInit.init();
        plan = t.getPlanMock();

        db = new SQLiteDB(plan, "debug" + MiscUtils.getTime());
        db.init();

        db.getServerTable().saveCurrentServerInfo(new ServerInfo(-1, TestInit.getServerUUID(), "ServerName", ""));

        when(plan.getDB()).thenReturn(db);
    }
}
