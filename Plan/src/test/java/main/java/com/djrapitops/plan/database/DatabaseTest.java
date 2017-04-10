/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.database;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;
import test.java.utils.TestInit;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.*;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.bukkit.scheduler.BukkitScheduler;
import org.easymock.EasyMock;
import static org.junit.Assert.assertTrue;
import test.java.utils.MockUtils;

/**
 *
 * @author Risto
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class, Bukkit.class, BukkitScheduler.class, BukkitRunnable.class})
public class DatabaseTest {

    private Plan plan;
    private Database db;
    private int rows;

    public DatabaseTest() {
    }

    @Before
    public void setUp() throws IOException, Exception {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        plan = t.getPlanMock();
        PowerMock.mockStatic(JavaPlugin.class);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
//        PowerMock.verify(JavaPlugin.class);      
        File f = new File(plan.getDataFolder(), "Errors.txt");
        rows = 0;
        if (f.exists()) {
            rows = Files.lines(f.toPath(), Charset.defaultCharset()).collect(Collectors.toList()).size();
        }
        db = new SQLiteDB(plan, "debug") {
            @Override
            public void startConnectionPingTask(Plan plugin) {

            }
        };
//        BukkitRunnable mockRunnable = PowerMockito.mock(BukkitRunnable.class);
//        when(mockRunnable.runTaskTimerAsynchronously(plan, anyLong(), anyLong())).thenReturn(null);
//        whenNew(BukkitRunnable.class).withNoArguments().thenReturn(mockRunnable);
//
        PowerMock.mockStatic(Bukkit.class);
        OfflinePlayer op = MockUtils.mockPlayer();
        EasyMock.expect(Bukkit.getOfflinePlayer(UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db"))).andReturn(op);
        op = MockUtils.mockPlayer2();        
        EasyMock.expect(Bukkit.getOfflinePlayer(UUID.fromString("ec94a954-1fa1-445b-b09b-9b698519af80"))).andReturn(op);
        PowerMock.replay(Bukkit.class);
//        BukkitScheduler mockScheduler = Mockito.mock(BukkitScheduler.class);
//        EasyMock.expect(Bukkit.getScheduler()).andReturn(mockScheduler);
    }

    @After
    public void tearDown() throws IOException, SQLException {
        db.close();
        File f = new File(plan.getDataFolder(), "Errors.txt");
        int rowsAgain = 0;
        if (f.exists()) {
            rowsAgain = Files.lines(f.toPath(), Charset.defaultCharset()).collect(Collectors.toList()).size();
        }
        assertTrue("Errors were caught.", rows == rowsAgain);
    }

    @Test
    public void testInit() {
        assertTrue("Database failed to init.", db.init());
    }

    @Test
    public void testRemoveAll() throws SQLException {
        db.init();
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        db.saveUserData(data.getUuid(), data);
        HashMap<String, Integer> c = new HashMap<>();
        c.put("/plan", 1);
        c.put("/tp", 4);
        c.put("/pla", 7);
        c.put("/help", 21);
        c.put("/roiergbnougbierubieugbeigubeigubgierbgeugeg", 3);
        db.saveCommandUse(c);
        db.removeAllData();
        assertTrue("Contains the user", db.getUserId(data.getUuid().toString()) == -1);
        assertTrue("Contains commandUse", db.getCommandUse().isEmpty());
    }

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
        assertTrue("Doesn't contain /plan", db.getCommandUse().containsKey("/plan"));
        assertTrue("Doesn't contain /tp", db.getCommandUse().containsKey("/tp"));
        assertTrue("Doesn't contain /pla", db.getCommandUse().containsKey("/pla"));
        assertTrue("Doesn't contain /help", db.getCommandUse().containsKey("/help"));
        assertTrue("Contains too long cmd", !db.getCommandUse().containsKey("/roiergbnougbierubieugbeigubeigubgierbgeugeg"));
    }

    @Test
    public void testSaveUserData() throws SQLException {
        db.init();
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        db.saveUserData(data.getUuid(), data);
        DBCallableProcessor process = new DBCallableProcessor() {
            @Override
            public void process(UserData d) {
                assertTrue("Not Equals", data.equals(d));
            }
        };
        db.giveUserDataToProcessors(data.getUuid(), process);
    }
    
    @Test
    public void testSaveMultipleUserData() throws SQLException {
        db.init();
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        UserData data2 = new UserData(MockUtils.mockPlayer2(), new DemographicsData());
        List<UserData> list = new ArrayList<>();
        list.add(data);
        list.add(data2);
        db.saveMultipleUserData(list);
        DBCallableProcessor process = new DBCallableProcessor() {
            @Override
            public void process(UserData d) {
                assertTrue("Not Equals", data.equals(d));
            }
        };
        db.giveUserDataToProcessors(data.getUuid(), process);
        DBCallableProcessor process2 = new DBCallableProcessor() {
            @Override
            public void process(UserData d) {
                assertTrue("Not Equals", data2.equals(d));
            }
        };
        db.giveUserDataToProcessors(data2.getUuid(), process2);
    }

    @Test
    public void testRemove() throws SQLException {
        db.init();
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        db.saveUserData(data.getUuid(), data);
        db.removeAccount(data.getUuid().toString());
        assertTrue("Contains the user", !db.wasSeenBefore(data.getUuid()));
    }
}
