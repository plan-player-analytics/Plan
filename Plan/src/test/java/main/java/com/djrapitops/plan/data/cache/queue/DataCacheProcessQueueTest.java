/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.cache.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.cache.queue.DataCacheProcessQueue;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.data.handling.info.InfoType;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
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
    public void setUp() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        Plan plan = t.getPlanMock();
        PowerMock.mockStatic(JavaPlugin.class);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
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

            @Override
            public void getUserDataForProcessing(DBCallableProcessor p, UUID uuid) {
                if (uuid.equals(MockUtils.getPlayerUUID())) {
                    OfflinePlayer op = MockUtils.mockPlayer();
                    UserData d = new UserData(op, new DemographicsData());
                    p.process(d);
                } else if (uuid.equals(MockUtils.getPlayer2UUID())) {
                    OfflinePlayer op = MockUtils.mockPlayer2();
                    UserData d = new UserData(op, new DemographicsData());
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
     *
     * @throws InterruptedException
     */
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
        assertTrue(q.stop().isEmpty());
        
    }

    /**
     *
     * @throws InterruptedException
     */
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
        assertTrue(q.stop().isEmpty());
    }

    /**
     *
     * @throws InterruptedException
     */
    @Ignore @Test
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
        Thread.sleep(2000);
        q.addToPool(h);
        assertTrue(q.containsUUID(uuid));
    }
}
