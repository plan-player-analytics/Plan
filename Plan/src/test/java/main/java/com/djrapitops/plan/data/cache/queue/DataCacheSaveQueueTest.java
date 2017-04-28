/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.cache.queue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheSaveQueue;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

/**
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class})
public class DataCacheSaveQueueTest {

    private Plan plan;
    private Database db;
    private boolean calledSaveUserData;
    private boolean calledSaveUserData2;

    /**
     *
     */
    public DataCacheSaveQueueTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        plan = t.getPlanMock();
        PowerMock.mockStatic(JavaPlugin.class);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
        calledSaveUserData = false;
        calledSaveUserData2 = false;
        db = new SQLiteDB(plan, "debug" + new Date().getTime()) {
            @Override
            public void startConnectionPingTask(Plan plugin) {

            }

            @Override
            public void saveUserData(UUID uuid, UserData data) throws SQLException {
                if (calledSaveUserData) {
                    calledSaveUserData2 = true;
                }
                calledSaveUserData = true;

            }
        };
        when(plan.getDB()).thenReturn(db);
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     *
     * @throws InterruptedException
     */
    @Test
    public void testScheduleForSave_UserData() throws InterruptedException {
        DataCacheSaveQueue q = new DataCacheSaveQueue(plan);
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        q.scheduleForSave(data);
        Thread.sleep(500);
        assertTrue(calledSaveUserData);
    }

    /**
     *
     * @throws InterruptedException
     */
    @Test
    public void testScheduleForSave_Collection() throws InterruptedException {
        DataCacheSaveQueue q = new DataCacheSaveQueue(plan);
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        UserData data2 = new UserData(MockUtils.mockPlayer2(), new DemographicsData());
        List<UserData> l = new ArrayList<>();
        l.add(data);
        l.add(data2);
        q.scheduleForSave(l);
        Thread.sleep(1000);
        assertTrue(calledSaveUserData);
        assertTrue(calledSaveUserData2);
    }

    /**
     *
     * @throws InterruptedException
     */
    @Test
    public void testScheduleNewPlayer() throws InterruptedException {
        DataCacheSaveQueue q = new DataCacheSaveQueue(plan);
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        q.scheduleNewPlayer(data);
        Thread.sleep(500);
        assertTrue(calledSaveUserData);
    }

    /**
     *
     */
    @Ignore("Inconsistant")
    @Test
    public void testContainsUUID() {
        DataCacheSaveQueue q = new DataCacheSaveQueue(plan);
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        q.stop();
        q.scheduleNewPlayer(data);
        assertTrue(q.containsUUID(data.getUuid()));
    }

    /**
     *
     * @throws InterruptedException
     */
    @Ignore
    @Test
    public void testStop() throws InterruptedException {
        DataCacheSaveQueue q = new DataCacheSaveQueue(plan);
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        q.stop();
        Thread.sleep(2000);
        q.scheduleNewPlayer(data);
        assertTrue(!calledSaveUserData);
    }

}
