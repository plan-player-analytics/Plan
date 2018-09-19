/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.tables.UsersTable;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.bukkit.BukkitCMDSender;
import org.bukkit.command.CommandSender;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.mocks.SystemMockUtil;
import utilities.mocks.objects.MockPlayers;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

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

//        Database.getActive().save().serverInfoForThisServer(new Server(-1, TestConstants.SERVER_UUID, "ServerName", "", 20));
    }

    @Before
    public void setUp() {
        db = null; // TODO;
        Assume.assumeNotNull(db);
    }

    @Test
    public void testGetPlayerDisplayNameArgsPerm() {
        String[] args = new String[]{"Rsl1122", "Test"};
        ISender sender = new BukkitCMDSender(MockPlayers.mockPlayer());

        String expResult = "Rsl1122";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    public void testGetPlayerDisplayNameArgsNoPerm() {
        String[] args = new String[]{"Rsl1122", "Test"};
        ISender sender = new BukkitCMDSender(MockPlayers.mockPlayer2());

        String result = MiscUtils.getPlayerName(args, sender);

        assertNull(result);
    }

    @Test
    public void testGetPlayerDisplayNameNoArgsPerm() {
        String[] args = new String[]{};
        ISender sender = new BukkitCMDSender(MockPlayers.mockPlayer());

        String expResult = "TestName";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    public void testGetPlayerDisplayNameNoArgsNoPerm() {
        String[] args = new String[]{};
        ISender sender = new BukkitCMDSender(MockPlayers.mockPlayer2());

        String expResult = "TestName2";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    public void testGetPlayerDisplayNameOwnNameNoPerm() {
        String[] args = new String[]{"testname2"};
        ISender sender = new BukkitCMDSender(MockPlayers.mockPlayer2());

        String expResult = "TestName2";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    @Test
    public void testGetPlayerDisplayNameConsole() {
        String[] args = new String[]{"TestConsoleSender"};
        ISender sender = new BukkitCMDSender(Mockito.mock(CommandSender.class));

        String expResult = "TestConsoleSender";
        String result = MiscUtils.getPlayerName(args, sender);

        assertEquals(expResult, result);
    }

    // TODO Move to database test
    @Test
    public void testGetMatchingNames() {
        String exp1 = "TestName";
        String exp2 = "TestName2";

        UsersTable usersTable = db.getUsersTable();
        UUID uuid1 = UUID.randomUUID();
        usersTable.registerUser(uuid1, 0L, exp1);
        usersTable.registerUser(UUID.randomUUID(), 0L, exp2);

        String search = "testname";

        List<String> result = db.search().matchingPlayers(search);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(exp1, result.get(0));
        assertEquals(exp2, result.get(1));
    }

    // TODO Move to database test
    @Test
    public void testGetMatchingNickNames() {
        UUID uuid = UUID.randomUUID();
        String userName = RandomData.randomString(10);
        db.getUsersTable().registerUser(uuid, 0L, userName);
        db.getUsersTable().registerUser(TestConstants.PLAYER_ONE_UUID, 1L, "Not random");

        String nickname = "2" + RandomData.randomString(10);
        db.getNicknamesTable().saveUserName(uuid, new Nickname(nickname, System.currentTimeMillis(), TestConstants.SERVER_UUID));
        db.getNicknamesTable().saveUserName(TestConstants.PLAYER_ONE_UUID, new Nickname("No nick", System.currentTimeMillis(), TestConstants.SERVER_UUID));

        String search = "2";

        List<String> result = db.search().matchingPlayers(search);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userName, result.get(0));
    }
}
