/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.data.cache;

import com.djrapitops.plan.system.cache.SessionCache;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.utilities.TestInit;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class SessionCacheTest {

    private SessionCache test;

    @Before
    public void setUp() throws Exception {
        TestInit.init();
    }

    @Test
    public void test() {
        // TODO Rewrite
    }
}
