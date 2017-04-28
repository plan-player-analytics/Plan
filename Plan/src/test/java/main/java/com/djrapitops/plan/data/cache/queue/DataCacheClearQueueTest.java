/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.cache.queue;

import java.io.File;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.TestInit;

/**
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class})
public class DataCacheClearQueueTest {
    
    private Plan plan;
    private DataCacheHandler handler;
    
    /**
     *
     */
    public DataCacheClearQueueTest() {
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
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
        File f = new File(plan.getDataFolder(), "Errors.txt");
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

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     *
     */
    @Test
    public void testScheduleForClear_UUID() {
    }

    /**
     *
     */
    @Test
    public void testScheduleForClear_Collection() {
    }

    /**
     *
     */
    @Test
    public void testStop() {
    }
    
}
