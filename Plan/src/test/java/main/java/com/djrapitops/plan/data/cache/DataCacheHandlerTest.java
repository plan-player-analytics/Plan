/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.cache;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

/**
 *
 * @author Risto
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

    public DataCacheHandlerTest() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        plan = t.getPlanMock();
        calledSaveCommandUse = false;
        calledSaveUserData = false;
        db = new SQLiteDB(plan, "debug" + new Date().getTime()) {
            @Override
            public void startConnectionPingTask(Plan plugin) {

            }

            @Override
            public void saveCommandUse(HashMap<String, Integer> c) {
                calledSaveCommandUse = true;
            }

            @Override
            public void saveUserData(UUID uuid, UserData data) throws SQLException {
                calledSaveUserData = true;
            }

            @Override
            public void saveMultipleUserData(List<UserData> data) throws SQLException {
                calledSaveMultiple = true;
            }
        };
        when(plan.getDB()).thenReturn(db);
        handler = new DataCacheHandler(plan) {            
            @Override
            public boolean getCommandUseFromDb() {                
                return true;
            }

            @Override
            public void startQueues() {
            }

            @Override
            public void startAsyncPeriodicSaveTask() throws IllegalArgumentException, IllegalStateException {
            }
        };
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Ignore
    @Test
    public void testGetUserDataForProcessing_3args() {
    }

    @Ignore
    @Test
    public void testGetUserDataForProcessing_DBCallableProcessor_UUID() {
    }

    @Test
    public void testSaveCachedUserData() {
        handler.saveCachedUserData();
        assertTrue("Didn't call saveMultiple", calledSaveMultiple);
    }

    @Ignore
    @Test
    public void testAddToPool() {
    }

    @Ignore
    @Test
    public void testSaveCacheOnDisable() {
    }

    @Ignore
    @Test
    public void testSaveCachedData() {
    }

    @Test
    public void testSaveCommandUse() {
        handler.saveCommandUse();
        assertTrue("Didn't call saveCMDUse for db", calledSaveCommandUse);
    }

    @Ignore
    @Test
    public void testSaveHandlerDataToCache() {
    }

    @Ignore
    @Test
    public void testClearCache() {
    }

    @Ignore("mock log")
    @Test
    public void testClearFromCache() {
        UUID uuid = MockUtils.getPlayerUUID();
        handler.getDataCache().put(uuid, null);
        handler.clearFromCache(uuid);
        assertTrue("Found uuid", !handler.getDataCache().containsKey(uuid));
    }

    @Test
    public void testScheludeForClear() {
    }

    @Test
    public void testIsDataAccessed() {
    }

    @Ignore
    @Test
    public void testNewPlayer_Player() {
    }

    @Ignore
    @Test
    public void testNewPlayer_OfflinePlayer() {
    }

    @Ignore
    @Test
    public void testNewPlayer_UserData() {
    }

    @Test
    public void testGetDataCache() {
        assertTrue("Cache was null", handler.getDataCache() != null);
    }

    @Ignore
    @Test
    public void testHandleReload() {
    }

    @Test
    public void testGetMaxPlayers() {
        assertEquals(20, handler.getMaxPlayers());
    }

    @Test
    public void testHandleCommand() {
        handler.handleCommand("/plan");
        assertEquals((Integer) 1, handler.getCommandUse().get("/plan"));
    }

}
