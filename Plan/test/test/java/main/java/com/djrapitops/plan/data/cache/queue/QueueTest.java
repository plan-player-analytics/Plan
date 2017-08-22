/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.cache.queue;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.DataCache;
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
import java.util.List;
import java.util.UUID;

import static org.powermock.api.mockito.PowerMockito.when;

// TODO Rewrite
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class})
public class QueueTest {

    private final UUID uuid1 = MockUtils.getPlayerUUID();

    private DataCache dataCache;
    private Database db;

    public QueueTest() {
    }

    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
        Plan plan = t.getPlanMock();
        db = new SQLiteDB(plan, "debug" + MiscUtils.getTime()) {
            @Override
            public void startConnectionPingTask() {
            }
        };
        db.init();
        when(plan.getDB()).thenReturn(db);
        dataCache = new DataCache(plan) {
            @Override
            public void startAsyncPeriodicSaveTask() {
            }
        };
        when(plan.getDataCache()).thenReturn(dataCache);
    }

    @After
    public void tearDown() throws SQLException {
        db.close();
    }

    @Test
    public void testProcessQueue() {
        List<Integer> processCalls = new ArrayList<>();
        List<Integer> errors = new ArrayList<>();
        // TODO Rewrite
    }
}
