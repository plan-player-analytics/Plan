/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.UsersTable;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.bukkit.BukkitCMDSender;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.mocks.SystemMockUtil;
import utilities.mocks.objects.MockUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class MiscUtilsTest {

    private SQLDB db;

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        StaticHolder.saveInstance(MiscUtils.class, Plan.class);
        SystemMockUtil.setUp(temporaryFolder.getRoot())
                .enableConfigSystem()
                .enableDatabaseSystem()
                .enableServerInfoSystem();

        Database.getActive().save().serverInfoForThisServer(new Server(-1, TestConstants.SERVER_UUID, "ServerName", "", 20));
    }

    @Before
    public void setUp() {
        db = (SQLDB) Database.getActive();
    }

    @Test
    public void testGetPlayerDisplayNameArgsPerm() {
        String[] args = new String[]{"Rsl1122", "Test"};
        ISender sender = new BukkitCMDSender(MockUtils.mockPlayer());

        String expResult = "Rsl1122";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    public void testGetPlayerDisplayNameArgsNoPerm() {
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
        String exp1 = "TestName";
        String exp2 = "TestName2";

        UsersTable usersTable = db.getUsersTable();
        UUID uuid1 = UUID.randomUUID();
        usersTable.registerUser(uuid1, 0L, exp1);
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
        UUID uuid = UUID.randomUUID();
        String userName = RandomData.randomString(10);
        db.getUsersTable().registerUser(uuid, 0L, userName);
        db.getUsersTable().registerUser(TestConstants.PLAYER_ONE_UUID, 1L, "Not random");

        String nickname = "2" + RandomData.randomString(10);
        db.getNicknamesTable().saveUserName(uuid, nickname);
        db.getNicknamesTable().saveUserName(TestConstants.PLAYER_ONE_UUID, "No nick");

        String search = "2";

        List<String> result = MiscUtils.getMatchingPlayerNames(search);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userName, result.get(0));
    }
}
