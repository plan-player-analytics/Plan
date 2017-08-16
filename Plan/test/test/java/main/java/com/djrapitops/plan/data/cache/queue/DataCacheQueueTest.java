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
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.TestInit;

import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class})
public class DataCacheQueueTest {

    private int calledSaveUserData = 0;

    public DataCacheQueueTest() {
    }

    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
        Plan plan = t.getPlanMock();
        Database db = new SQLiteDB(plan, "debug" + MiscUtils.getTime()) {
            @Override
            public void startConnectionPingTask() {
            }

            @Override
            public void convertBukkitDataToDB() {
            }

            private UserData getData(UUID uuid) {
                UUID one = MockUtils.getPlayerUUID();
                UserData data;
                if (uuid.equals(one)) {
                    data = MockUtils.mockUserWithMoreData();
                } else {
                    data = new UserData(MockUtils.mockIPlayer2());
                }
                return data;
            }

            @Override
            public void giveUserDataToProcessors(UUID uuid, Collection<DBCallableProcessor> processors) throws SQLException {
                UserData data = getData(uuid);
                processors.forEach(processor -> processor.process(data));
            }

            @Override
            public void saveUserData(UserData data) throws SQLException {
                calledSaveUserData++;
            }
        };
        when(plan.getDB()).thenReturn(db);
        DataCacheHandler handler = new DataCacheHandler(plan) {
            @Override
            public void startAsyncPeriodicSaveTask() {
            }
        };
        when(plan.getHandler()).thenReturn(handler);
    }

    @After
    public void tearDown() {
    }


}
