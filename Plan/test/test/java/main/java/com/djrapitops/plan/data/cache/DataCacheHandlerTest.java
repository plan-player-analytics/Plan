/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.cache;

import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.database.Database;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class DataCacheHandlerTest {

    private Database db;
    private DataCacheHandler handler;

    private int callsToSaveCommandUse;
    private int callsToSaveUserData;
    private int callsToSaveMultiple;

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

    }

    @After
    public void tearDown() {

    }
}
