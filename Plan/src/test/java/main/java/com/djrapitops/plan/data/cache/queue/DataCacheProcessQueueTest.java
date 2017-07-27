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
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheProcessQueue;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.data.handling.info.InfoType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class})
public class DataCacheProcessQueueTest {

    private DataCacheHandler handler;

    /**
     *
     */
    public DataCacheProcessQueueTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
        handler = new DataCacheHandler(Plan.getInstance()) {
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

            @Override
            public void getUserDataForProcessing(DBCallableProcessor p, UUID uuid) {
                if (uuid.equals(MockUtils.getPlayerUUID())) {
                    UserData d = MockUtils.mockUser();
                    p.process(d);
                } else if (uuid.equals(MockUtils.getPlayer2UUID())) {
                    UserData d = MockUtils.mockUser2();
                    p.process(d);
                }
            }
        };
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     * @throws InterruptedException
     */
    @Ignore("Scheduler")
    @Test
    public void testAddToPool_HandlingInfo() throws InterruptedException {
        DataCacheProcessQueue q = new DataCacheProcessQueue(handler);
        UUID uuid = MockUtils.getPlayerUUID();
        q.addToPool(new HandlingInfo(uuid, InfoType.CHAT, 0) {
            @Override
            public boolean process(UserData uData) {
                assertEquals(uuid, uData.getUuid());
                return true;
            }
        });
        Thread.sleep(1000);
        assertTrue(q.stopAndReturnLeftovers().isEmpty());

    }

    /**
     * @throws InterruptedException
     */
    @Ignore("Scheduler")
    @Test
    public void testAddToPool_Collection() throws InterruptedException {
        DataCacheProcessQueue q = new DataCacheProcessQueue(handler);
        UUID uuid = MockUtils.getPlayerUUID();
        HandlingInfo h = new HandlingInfo(uuid, InfoType.CHAT, 0) {
            @Override
            public boolean process(UserData uData) {
                assertEquals(uuid, uData.getUuid());
                return true;
            }
        };
        List<HandlingInfo> l = new ArrayList<>();
        l.add(h);
        l.add(h);
        l.add(h);
        q.addToPool(l);
        Thread.sleep(1000);
        assertTrue(q.stopAndReturnLeftovers().isEmpty());
    }

    /**
     * @throws InterruptedException
     */
    @Ignore("Inconsistant")
    @Test
    public void testContainsUUID() throws InterruptedException {
        DataCacheProcessQueue q = new DataCacheProcessQueue(handler);
        UUID uuid = MockUtils.getPlayerUUID();
        HandlingInfo h = new HandlingInfo(uuid, InfoType.CHAT, 0) {
            @Override
            public boolean process(UserData uData) {
                assertEquals(uuid, uData.getUuid());
                return true;
            }
        };
        q.stop();
        q.addToPool(h);
        assertTrue(q.containsUUID(uuid));
    }
}
