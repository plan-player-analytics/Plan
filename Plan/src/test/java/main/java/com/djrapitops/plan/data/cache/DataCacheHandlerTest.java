/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.cache;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@PrepareForTest(JavaPlugin.class)
public class DataCacheHandlerTest {

    private Plan plan;
    private Database db;
    private DataCacheHandler handler;
    private boolean calledSaveCommandUse;
    private boolean calledSaveUserData;
    private boolean calledSaveMultiple;

    /**
     *
     */
    public DataCacheHandlerTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        plan = t.getPlanMock();
        calledSaveCommandUse = false;
        calledSaveUserData = false;
        db = new SQLiteDB(plan, "debug" + MiscUtils.getTime()) {
            @Override
            public void startConnectionPingTask(Plan plugin) {

            }

            @Override
            public void convertBukkitDataToDB() {

            }

            @Override
            public HashMap<String, Integer> getCommandUse() {
                return new HashMap<>();
            }

            @Override
            public void giveUserDataToProcessors(UUID uuid, Collection<DBCallableProcessor> processors) {
                if (uuid.equals(MockUtils.getPlayerUUID())) {
                    OfflinePlayer op = MockUtils.mockPlayer();
                    UserData d = new UserData(op, new DemographicsData());
                    for (DBCallableProcessor p : processors) {
                        p.process(d);
                    }
                } else if (uuid.equals(MockUtils.getPlayer2UUID())) {
                    OfflinePlayer op = MockUtils.mockPlayer2();
                    UserData d = new UserData(op, new DemographicsData());
                    for (DBCallableProcessor p : processors) {
                        p.process(d);
                    }
                }
            }

            @Override
            public void saveCommandUse(Map<String, Integer> c) {
                calledSaveCommandUse = true;
            }

            @Override
            public void saveUserData(UserData data) throws SQLException {
                calledSaveUserData = true;
            }

            @Override
            public void saveMultipleUserData(Collection<UserData> data) throws SQLException {
                calledSaveMultiple = true;
            }
        };
        when(plan.getDB()).thenReturn(db);
        handler = new DataCacheHandler(plan) {
            @Override
            public void startAsyncPeriodicSaveTask() throws IllegalArgumentException, IllegalStateException {
            }
        };
    }

    /**
     *
     * @throws SQLException
     */
    @After
    public void tearDown() throws SQLException {
//        db.close();
    }

    /**
     *
     * @throws SQLException
     * @throws InterruptedException
     */
    @Ignore("Scheduler")
    @Test
    public void testGetUserDataForProcessingCache() throws SQLException, InterruptedException {
//        db.init();
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        db.saveUserData(data);
        handler.getUserDataForProcessing(new DBCallableProcessor() {
            @Override
            public void process(UserData d) {
                assertTrue(d.equals(data));
            }
        }, data.getUuid());
        Thread.sleep(1000);
        assertTrue(handler.getDataCache().containsKey(data.getUuid()));
        assertTrue(handler.getDataCache().get(data.getUuid()).equals(data));
    }

    /**
     *
     * @throws SQLException
     * @throws InterruptedException
     */
    @Ignore
    @Test
    public void testGetUserDataForProcessingDontCache() throws SQLException, InterruptedException {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        db.saveUserData(data);
        handler.getUserDataForProcessing(new DBCallableProcessor() {
            @Override
            public void process(UserData d) {
                assertTrue(d.equals(data));
            }
        }, data.getUuid(), false);
        Thread.sleep(1000);
        assertTrue(!handler.getDataCache().containsKey(data.getUuid()));
        assertTrue(handler.getDataCache().get(data.getUuid()) == null);
    }

    /**
     *
     */
    @Ignore("Scheduler")
    @Test
    public void testSaveCachedUserData() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        handler.getDataCache().put(data.getUuid(), data);
        handler.saveCachedUserData();
        assertTrue("Didn't call saveMultiple", calledSaveMultiple);
    }

    /**
     *
     */
    @Ignore
    @Test
    public void testAddToPool() {
    }

    /**
     *
     */
    @Ignore
    @Test
    public void testSaveCacheOnDisable() {
        UserData data = new UserData(MockUtils.mockPlayer(), new DemographicsData());
        handler.getDataCache().put(data.getUuid(), data);
        handler.startSession(data.getUuid());
        handler.saveCacheOnDisable();
        assertTrue(calledSaveMultiple);
    }

    /**
     *
     */
    @Ignore
    @Test
    public void testSaveCachedData() {
    }

    /**
     *
     */
    @Ignore("Scheduler")
    @Test
    public void testSaveCommandUse() {
        handler.saveCommandUse();
        assertTrue("Didn't call saveCMDUse for db", calledSaveCommandUse);
    }

    /**
     *
     */
    @Ignore
    @Test
    public void testSaveHandlerDataToCache() {
    }

    /**
     *
     */
    @Ignore
    @Test
    public void testClearCache() {
    }

    /**
     *
     */
    @Ignore("mock log")
    @Test
    public void testClearFromCache() {
        UUID uuid = MockUtils.getPlayerUUID();
        handler.getDataCache().put(uuid, null);
        handler.clearFromCache(uuid);
        assertTrue("Found uuid", !handler.getDataCache().containsKey(uuid));
        assertTrue("Not empty", handler.getDataCache().isEmpty());
    }

    /**
     *
     */
    @Ignore
    @Test
    public void testScheludeForClear() {
    }

    /**
     *
     */
    @Ignore
    @Test
    public void testIsDataAccessed() {
    }

    /**
     *
     */
    @Ignore
    @Test
    public void testNewPlayer_Player() {
    }

    /**
     *
     */
    @Ignore
    @Test
    public void testNewPlayer_OfflinePlayer() {
    }

    /**
     *
     */
    @Ignore
    @Test
    public void testNewPlayer_UserData() {
    }

    /**
     *
     */
    @Ignore("Scheduler")
    @Test
    public void testGetDataCache() {
        assertTrue("Cache was null", handler.getDataCache() != null);
    }

    /**
     *
     */
    @Ignore
    @Test
    public void testHandleReload() {
    }

    /**
     *
     */
    @Ignore("Scheduler")
    @Test
    public void testHandleCommand() {
        handler.handleCommand("/plan");
        assertEquals((Integer) 1, handler.getCommandUse().get("/plan"));
    }

}
