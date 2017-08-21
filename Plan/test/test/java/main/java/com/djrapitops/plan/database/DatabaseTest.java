/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.database;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.database.tables.TPSTable;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import org.bukkit.plugin.java.JavaPlugin;
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
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
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
        };
        File f = new File(plan.getDataFolder(), "Errors.txt");
        rows = FileUtil.lines(f).size();
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

        List<String> lines = FileUtil.lines(f);
        int rowsAgain = lines.size();
        if (rowsAgain > 0) {
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
        }.getName());
    }

    /**
     * @throws SQLException
     */
    @Test // TODO Rewrite
    public void testRemoveAll() throws SQLException {
        db.init();
//        UserData data = MockUtils.mockUser();
//        db.saveUserData(data);
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
    @Test // TODO Rewrite
    public void testRemove() throws SQLException {
        db.init();
//        UserData data = MockUtils.mockUser();
//        db.saveUserData(data);
//        assertTrue(db.removeAccount(data.getUuid().toString()));
//        assertTrue("Contains the user", !db.wasSeenBefore(data.getUuid()));
    }

    /**
     * @throws SQLException
     */
    @Test
    @Ignore("Backup has to be rewritten")
    public void testBackup() throws SQLException {
    }

    /**
     * @throws SQLException
     */
    // Big test because
    @Test
    @Ignore("Backup has to be rewritten")
    public void testRestore() throws SQLException {

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

        final long usedMemory = 51231251254L;
        final int entityCount = 6123;
        final int chunksLoaded = 2134;

        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));
        expected.add(new TPS(r.nextLong(), r.nextDouble(), r.nextInt(100000000), averageCPUUsage, usedMemory, entityCount, chunksLoaded));

        tpsTable.saveTPSData(expected);
        assertEquals(expected, tpsTable.getTPSData());
    }
}
