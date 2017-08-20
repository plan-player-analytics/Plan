/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.cache.queue;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.data.handling.info.InfoType;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class})
public class DataCacheQueueTest {

    private final UUID uuid1 = MockUtils.getPlayerUUID();
    private final UserData data1 = MockUtils.mockUserWithMoreData();
    private final UserData data2 = new UserData(MockUtils.mockIPlayer2());

    private int callsToSaveUserData;
    private int callsToGetUserData;

    private DataCacheHandler handler;
    private Database db;

    public DataCacheQueueTest() {
    }

    @Before
    public void setUp() throws Exception {
        callsToSaveUserData = 0;
        callsToGetUserData = 0;
        TestInit t = TestInit.init();
        Plan plan = t.getPlanMock();
        db = new SQLiteDB(plan, "debug" + MiscUtils.getTime()) {
            @Override
            public void startConnectionPingTask() {
            }

            @Override
            public void convertBukkitDataToDB() {
            }

            private UserData getData(UUID uuid) {
                UserData data;
                if (uuid.equals(uuid1)) {
                    data = data1;
                } else {
                    data = data2;
                }
                return data;
            }

            @Override
            public void giveUserDataToProcessors(UUID uuid, Collection<DBCallableProcessor> processors) throws SQLException {
                callsToGetUserData++;
                UserData data = getData(uuid);
                processors.forEach(processor -> processor.process(data));
            }

            @Override
            public void saveUserData(UserData data) throws SQLException {
                callsToSaveUserData++;
            }
        };
        db.init();
        when(plan.getDB()).thenReturn(db);
        handler = new DataCacheHandler(plan) {
            @Override
            public void startAsyncPeriodicSaveTask() {
            }
        };
        when(plan.getHandler()).thenReturn(handler);
    }

    @After
    public void tearDown() throws SQLException {
        db.close();
    }

    @Test
    public void testGetQueue_cache() {
        List<Integer> calls = new ArrayList<>();
        List<Integer> errors = new ArrayList<>();
        handler.getUserDataForProcessing(data -> {
            if (data.equals(data1)) {
                calls.add(1);
            } else {
                errors.add(1);
            }
        }, uuid1);
        while (calls.size() < 1) {
            if (errors.size() > 0) {
                fail();
            }
        }
        assertEquals(1, calls.size());
        assertEquals(0, errors.size());
        assertEquals(1, callsToGetUserData);
        assertTrue(handler.getDataCache().containsKey(uuid1));
    }

    @Test
    public void testGetQueue_dontCache() {
        List<Integer> getCalls = new ArrayList<>();
        List<Integer> errors = new ArrayList<>();
        handler.getUserDataForProcessing(data -> {
            if (data.equals(data1)) {
                getCalls.add(1);
            } else {
                errors.add(1);
            }
        }, uuid1, false);
        while (getCalls.size() < 1) {
            if (errors.size() > 0) {
                fail();
            }
        }
        assertEquals(1, getCalls.size());
        assertEquals(0, errors.size());
        assertEquals(1, callsToGetUserData);
        assertTrue(!handler.getDataCache().containsKey(uuid1));
    }

    @Test
    public void testProcessQueue() {
        List<Integer> processCalls = new ArrayList<>();
        List<Integer> errors = new ArrayList<>();
        handler.addToPool(new HandlingInfo(uuid1, InfoType.OTHER, 0) {
            @Override
            public void process(UserData uData) {
                if (uData.equals(data1)) {
                    uData.setName("TestSuccessful");
                    processCalls.add(1);
                } else {
                    errors.add(1);
                }
            }
        });
        while (processCalls.size() < 1) {
            if (errors.size() > 0) {
                fail();
            }
        }
        assertEquals(1, processCalls.size());
        assertEquals(0, errors.size());
        assertEquals(1, callsToGetUserData);
        assertTrue(handler.getDataCache().containsKey(uuid1));
        assertEquals("TestSuccessful", handler.getDataCache().get(uuid1).getName());
    }

    // TODO Save & Clear Queue tests
}
