/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.data.cache;

import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.database.databases.Database;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class DataCacheTest {

    private Database db;
    private DataCache dataCache;

    private int callsToSaveCommandUse;
    private int callsToSaveUserData;
    private int callsToSaveMultiple;

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Ignore
    @Test
    public void testHandler() {
        // TODO
    }
}
