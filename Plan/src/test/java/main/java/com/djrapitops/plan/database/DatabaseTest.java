/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.database;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.database.tables.TPSTable;
import main.java.com.djrapitops.plan.utilities.ManageUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
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
     * @throws IOException
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
        plan = t.getPlanMock();
        db = new SQLiteDB(plan, "debug" + MiscUtils.getTime()) {
            @Override
            public void startConnectionPingTask() {

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
    }

    /**
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
            public void startConnectionPingTask() {

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
            public void startConnectionPingTask() {

            }

            @Override
            public void convertBukkitDataToDB() {

            }
        }.getName());
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testRemoveAll() throws SQLException {
        db.init();
        UserData data = MockUtils.mockUser();
        db.saveUserData(data);
        HashMap<String, Integer> c = new HashMap<>();
        c.put("/plan", 1);
        c.put("/tp", 4);
        c.put("/pla", 7);
        c.put("/help", 21);
        c.put("/roiergbnougbierubieugbeigubeigubgierbgeugeg", 3);
        db.saveCommandUse(c);
        assertTrue(db.removeAllData());
        assertTrue("Contains the user", db.getUserDataForUUIDS(Arrays.asList(MockUtils.getPlayerUUID(), MockUtils.getPlayer2UUID())).isEmpty());
        assertTrue("Contains commandUse", db.getCommandUse().isEmpty());
    }

    /**
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
        c.remove("/roiergbnougbierubieugbeigubeigubgierbgeugeg");
        Map<String, Integer> commandUse = db.getCommandUse();
        assertEquals(c, commandUse);
        c.put("/test", 3);
        c.put("/tp", 6);
        c.put("/pla", 4);
        db.saveCommandUse(c);
        c.put("/pla", 7);
        commandUse = db.getCommandUse();
        assertEquals(c, commandUse);
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testSaveUserData() throws SQLException {
        db.init();
        UserData data = MockUtils.mockUser();
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
     * @throws SQLException
     */
    @Test
    public void testNicknameInjection() throws SQLException {
        db.init();
        UserData data = MockUtils.mockUser();
        UserData data2 = MockUtils.mockUser2();
        db.saveUserData(data2);
        data.addNickname("s); DROP TABLE plan_users;--");
        db.saveUserData(data);
        assertTrue("Removed Users table.", db.getUsersTable().getUserId(data2.getUuid().toString()) != -1);
    }

    /**
     * @throws SQLException
     * @throws java.net.UnknownHostException
     */
    @Test
    public void testSaveMultipleUserData() throws SQLException, UnknownHostException {
        db.init();
        UserData data = MockUtils.mockUser();
        data.addIpAddress(InetAddress.getByName("185.64.113.61"));
        data.addSession(new SessionData(1286349L, 2342978L));
        data.addNickname("TestNick");
        data.addPlayerKill(new KillData(MockUtils.getPlayer2UUID(), 2, "DiamondSword", 75843759L));
        System.out.println(data.toString());
        db.saveUserData(data);
        data.getPlayerKills().clear();
        System.out.println(data.toString());
        data.addNickname("TestUpdateForSave");
        UserData data2 = MockUtils.mockUser2();
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
     * @throws SQLException
     */
    @Test
    public void testRemove() throws SQLException {
        db.init();
        UserData data = MockUtils.mockUser();
        db.saveUserData(data);
        assertTrue(db.removeAccount(data.getUuid().toString()));
        assertTrue("Contains the user", !db.wasSeenBefore(data.getUuid()));
    }

    /**
     * @throws SQLException
     */
    @Test
    public void testBackup() throws SQLException {
        db.init();
        UserData data = MockUtils.mockUser();
        UserData data2 = MockUtils.mockUser2();
        List<UserData> list = new ArrayList<>();
        list.add(data);
        list.add(data2);
        db.saveMultipleUserData(list);
        backup = new SQLiteDB(plan, "debug-backup") {
            @Override
            public void startConnectionPingTask() {

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
     * @throws SQLException
     */
    // Big test because
    @Test
    public void testRestore() throws SQLException {
        db.init();
        UserData data = MockUtils.mockUser();
        UserData data2 = MockUtils.mockUser2();
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
            public void startConnectionPingTask() {

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

    @Test
    public void testTPSSaving() throws SQLException {
        db.init();
        TPSTable tpsTable = db.getTpsTable();
        List<TPS> expected = new ArrayList<>();
        Random r = new Random();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        final double averageCPUUsage = MathUtils.round(operatingSystemMXBean.getSystemLoadAverage() / availableProcessors * 100.0);
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage));
        tpsTable.saveTPSData(expected);
        assertEquals(expected, tpsTable.getTPSData());
    }

    @Test
    @Ignore("Changed clean limit.")
    public void testTPSClean() throws SQLException {
        db.init();
        TPSTable tpsTable = db.getTpsTable();
        List<TPS> expected = new ArrayList<>();
        Random r = new Random();
        long now = System.currentTimeMillis();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        int availableProcessors = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        final double averageCPUUsage = MathUtils.round(operatingSystemMXBean.getSystemLoadAverage() / availableProcessors * 100.0);
        expected.add(new TPS(now, r.nextDouble(), r.nextInt(100000000), averageCPUUsage));
        expected.add(new TPS(now - 1000L, r.nextDouble(), r.nextInt(100000000), averageCPUUsage));
        expected.add(new TPS(now - 3000L, r.nextDouble(), r.nextInt(100000000), averageCPUUsage));
        expected.add(new TPS(now - (690000L * 1000L), r.nextDouble(), r.nextInt(100000000), averageCPUUsage));
        TPS tooOldTPS = new TPS(now - (691400L * 1000L), r.nextDouble(), r.nextInt(100000000), averageCPUUsage);
        expected.add(tooOldTPS);
        tpsTable.saveTPSData(expected);
        tpsTable.clean();
        expected.remove(tooOldTPS);
        assertEquals(expected, tpsTable.getTPSData());
    }
}
