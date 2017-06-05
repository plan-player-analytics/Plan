/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.database;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.utilities.ManageUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.*;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class, Bukkit.class, BukkitScheduler.class, BukkitRunnable.class})
public class DatabaseTest {

    private Plan plan;
    private Database db;
    private Database backup;
    private int rows;

    /**
     *
     */
    public DatabaseTest() {
    }

    /**
     *
     * @throws IOException
     * @throws Exception
     */
    @Before
    public void setUp() throws IOException, Exception {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        plan = t.getPlanMock();
        db = new SQLiteDB(plan, "debug" + MiscUtils.getTime()) {
            @Override
            public void startConnectionPingTask(Plan plugin) {

            }
            
            @Override
            public void convertBukkitDataToDB() {
                
            }
        };
        File f = new File(plan.getDataFolder(), "Errors.txt");
        rows = 0;
        if (f.exists()) {
            rows = Files.lines(f.toPath(), Charset.defaultCharset()).collect(Collectors.toList()).size();
        }
//        BukkitRunnable mockRunnable = PowerMockito.mock(BukkitRunnable.class);
//        when(mockRunnable.runTaskTimerAsynchronously(plan, anyLong(), anyLong())).thenReturn(null);
//        whenNew(BukkitRunnable.class).withNoArguments().thenReturn(mockRunnable);
//
        PowerMock.mockStatic(Bukkit.class);
        OfflinePlayer op = MockUtils.mockPlayer();
        EasyMock.expect(Bukkit.getOfflinePlayer(UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db"))).andReturn(op);
        EasyMock.expect(Bukkit.getOfflinePlayer(UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db"))).andReturn(op);
        EasyMock.expect(Bukkit.getOfflinePlayer(UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db"))).andReturn(op);
        EasyMock.expect(Bukkit.getOfflinePlayer(UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db"))).andReturn(op);
        OfflinePlayer op2 = MockUtils.mockPlayer2();
        EasyMock.expect(Bukkit.getOfflinePlayer(UUID.fromString("ec94a954-1fa1-445b-b09b-9b698519af80"))).andReturn(op2);
        EasyMock.expect(Bukkit.getOfflinePlayer(UUID.fromString("ec94a954-1fa1-445b-b09b-9b698519af80"))).andReturn(op2);
        EasyMock.expect(Bukkit.getOfflinePlayer(UUID.fromString("ec94a954-1fa1-445b-b09b-9b698519af80"))).andReturn(op2);
        EasyMock.expect(Bukkit.getOfflinePlayer(UUID.fromString("ec94a954-1fa1-445b-b09b-9b698519af80"))).andReturn(op2);
        EasyMock.expect(Bukkit.getOfflinePlayers()).andReturn(new OfflinePlayer[]{op, op2});
        EasyMock.expect(Bukkit.getOfflinePlayers()).andReturn(new OfflinePlayer[]{op, op2});
        EasyMock.expect(Bukkit.getOfflinePlayers()).andReturn(new OfflinePlayer[]{op, op2});
        EasyMock.expect(Bukkit.getOfflinePlayers()).andReturn(new OfflinePlayer[]{op, op2});
        PowerMock.replay(Bukkit.class);
//        BukkitScheduler mockScheduler = Mockito.mock(BukkitScheduler.class);
//        EasyMock.expect(Bukkit.getScheduler()).andReturn(mockScheduler);
    }

    /**
     *
     * @throws IOException
     * @throws SQLException
     */
    @After
    public void tearDown() throws IOException, SQLException {
        db.close();
        if (backup != null) {
            backup.close();
        }
        File f = new File(plan.getDataFolder(), "Errors.txt");
        int rowsAgain = 0;
        if (f.exists()) {
            List<String> lines = Files.lines(f.toPath(), Charset.defaultCharset()).collect(Collectors.toList());
            rowsAgain = lines.size();
            for (String line : lines) {
                System.out.println(line);
            }
        }
        assertTrue("Errors were caught.", rows == rowsAgain);
    }

    /**
     *
     */
    @Test
    public void testInit() {
        assertTrue("Database failed to init.", db.init());
    }

    /**
     *
     */
    @Test
    public void testSqLiteGetConfigName() {
        assertEquals("sqlite", db.getConfigName());
    }

    /**
     *
     */
    @Test
    public void testSqLiteGetgName() {
        assertEquals("SQLite", db.getName());
    }

    /**
     *
     */
    @Test
    public void testMysqlGetConfigName() {
        assertEquals("mysql", new MySQLDB(plan) {
            @Override
            public void startConnectionPingTask(Plan plugin) {

            }
            @Override
            public void convertBukkitDataToDB() {
                
            }
        }.getConfigName());
    }

    /**
     *
     */
    @Test
    public void testMysqlGetName() {
        assertEquals("MySQL", new MySQLDB(plan) {
            @Override
            public void startConnectionPingTask(Plan plugin) {

            }
            @Override
            public void convertBukkitDataToDB() {
                
            }
        }.getName());
    }

    /**
     *
     * @throws SQLException
     */
    @Test
    public void testRemoveAll() throws SQLException {
        db.init();
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        db.saveUserData(data);
        HashMap<String, Integer> c = new HashMap<>();
        c.put("/plan", 1);
        c.put("/tp", 4);
        c.put("/pla", 7);
        c.put("/help", 21);
        c.put("/roiergbnougbierubieugbeigubeigubgierbgeugeg", 3);
        db.saveCommandUse(c);
        assertTrue(db.removeAllData());
        assertTrue("Contains the user", db.getUserDataForUUIDS(Arrays.asList(new UUID[]{MockUtils.getPlayerUUID(), MockUtils.getPlayer2UUID()})).isEmpty());
        assertTrue("Contains commandUse", db.getCommandUse().isEmpty());
    }

    /**
     *
     * @throws SQLException
     */
    @Test
    public void testSaveCommandUse() throws SQLException {
        db.init();
        HashMap<String, Integer> c = new HashMap<>();
        c.put("/plan", 1);
        c.put("/tp", 4);
        c.put("/pla", 7);
        c.put("/help", 21);
        c.put("/roiergbnougbierubieugbeigubeigubgierbgeugeg", 3);
        db.saveCommandUse(c);
        Map<String, Integer> commandUse = db.getCommandUse();
        assertTrue("Doesn't contain /plan", commandUse.containsKey("/plan"));
        assertTrue("Doesn't contain /tp", commandUse.containsKey("/tp"));
        assertTrue("Doesn't contain /pla", commandUse.containsKey("/pla"));
        assertTrue("Doesn't contain /help", commandUse.containsKey("/help"));
        assertTrue("Contains too long cmd", !commandUse.containsKey("/roiergbnougbierubieugbeigubeigubgierbgeugeg"));
    }

    /**
     *
     * @throws SQLException
     */
    @Test
    public void testSaveUserData() throws SQLException {
        db.init();
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        db.saveUserData(data);
        data.addNickname("TestUpdateForSave");
        db.saveUserData(data);
        DBCallableProcessor process = new DBCallableProcessor() {
            @Override
            public void process(UserData d) {
                assertTrue("Not Equals", data.equals(d));
            }
        };
        db.giveUserDataToProcessors(data.getUuid(), process);
    }

    /**
     *
     * @throws SQLException
     */
    @Test
    public void testNicknameInjection() throws SQLException {
        db.init();
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        UserData data2 = new UserData(MockUtils.mockPlayer2(), new DemographicsData());
        db.saveUserData(data2);
        data.addNickname("s); DROP TABLE plan_users;--");
        db.saveUserData(data);
        assertTrue("Removed Users table.", db.getUsersTable().getUserId(data2.getUuid().toString()) != -1);
    }

    /**
     *
     * @throws SQLException
     * @throws java.net.UnknownHostException
     */
    @Test
    public void testSaveMultipleUserData() throws SQLException, UnknownHostException {
        db.init();
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        data.addIpAddress(InetAddress.getByName("185.64.113.61"));
        data.addSession(new SessionData(1286349L, 2342978L));
        data.addNickname("TestNick");
        data.addPlayerKill(new KillData(MockUtils.getPlayer2UUID(), 2, "DiamondSword", 75843759L));
        System.out.println(data.toString());
        db.saveUserData(data);
        data.getPlayerKills().clear();
        System.out.println(data.toString());
        data.addNickname("TestUpdateForSave");
        UserData data2 = new UserData(MockUtils.mockPlayer2(), new DemographicsData());
        data2.addNickname("Alright");
        data.addNickname("TestNick2");
        data2.addIpAddress(InetAddress.getByName("185.64.113.60"));
        data2.addSession(new SessionData(2348743L, 4839673L));
        data2.addPlayerKill(new KillData(MockUtils.getPlayerUUID(), 1, "DiamondSword", 753759L));
        List<UserData> list = new ArrayList<>();
        list.add(data);
        list.add(data2);
        db.saveMultipleUserData(list);
        data.addPlayerKill(new KillData(MockUtils.getPlayer2UUID(), 2, "DiamondSword", 75843759L));
        data.setLocation(null);
        data2.setLocation(null);        
        DBCallableProcessor process = new DBCallableProcessor() {
            @Override
            public void process(UserData d) {
                System.out.println("\n" + data.toString());
                System.out.println(d.toString());
                assertTrue("Not Equals", data.equals(d));
            }
        };
        db.giveUserDataToProcessors(data.getUuid(), process);
        DBCallableProcessor process2 = new DBCallableProcessor() {
            @Override
            public void process(UserData d) {
                System.out.println("\n" + data2.toString());
                System.out.println(d.toString());
                assertTrue("Not Equals", data2.equals(d));
            }
        };
        db.giveUserDataToProcessors(data2.getUuid(), process2);
    }

    /**
     *
     * @throws SQLException
     */
    @Test
    public void testRemove() throws SQLException {
        db.init();
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        db.saveUserData(data);
        assertTrue(db.removeAccount(data.getUuid().toString()));
        assertTrue("Contains the user", !db.wasSeenBefore(data.getUuid()));
    }

    /**
     *
     * @throws SQLException
     */
    @Test
    public void testBackup() throws SQLException {
        db.init();
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        UserData data2 = new UserData(MockUtils.mockPlayer2(), new DemographicsData());
        List<UserData> list = new ArrayList<>();
        list.add(data);
        list.add(data2);
        db.saveMultipleUserData(list);
        backup = new SQLiteDB(plan, "debug-backup") {
            @Override
            public void startConnectionPingTask(Plan plugin) {

            }
            @Override
            public void convertBukkitDataToDB() {
                
            }
        };
        backup.init();
        ManageUtils.clearAndCopy(backup, db, db.getSavedUUIDs());
        Set<UUID> savedUUIDs = backup.getSavedUUIDs();
        assertTrue("Didn't contain 1", savedUUIDs.contains(data.getUuid()));
        assertTrue("Didn't contain 2", savedUUIDs.contains(data2.getUuid()));
    }

    /**
     *
     * @throws SQLException
     */
    // Big test because
    @Test
    public void testRestore() throws SQLException {
        db.init();
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        UserData data2 = new UserData(MockUtils.mockPlayer2(), new DemographicsData());
        List<UserData> list = new ArrayList<>();
        list.add(data);
        list.add(data2);
        db.saveMultipleUserData(list);
        HashMap<String, Integer> c = new HashMap<>();
        c.put("/plan", 1);
        c.put("/tp", 4);
        c.put("/pla", 7);
        c.put("/help", 21);
        db.saveCommandUse(c);
        backup = new SQLiteDB(plan, "debug-backup") {
            @Override
            public void startConnectionPingTask(Plan plugin) {

            }
            @Override
            public void convertBukkitDataToDB() {
                
            }
        };
        backup.init();
        ManageUtils.clearAndCopy(backup, db, db.getSavedUUIDs());
        ManageUtils.clearAndCopy(db, backup, backup.getSavedUUIDs());
        Set<UUID> savedUUIDs = db.getSavedUUIDs();
        assertTrue("Didn't contain 1", savedUUIDs.contains(data.getUuid()));
        assertTrue("Didn't contain 2", savedUUIDs.contains(data2.getUuid()));
        Map<String, Integer> commandUse = db.getCommandUse();
        assertTrue("Doesn't contain /plan", commandUse.containsKey("/plan"));
        assertTrue("Doesn't contain /tp", commandUse.containsKey("/tp"));
        assertTrue("Doesn't contain /pla", commandUse.containsKey("/pla"));
        assertTrue("Doesn't contain /help", commandUse.containsKey("/help"));
    }
}
