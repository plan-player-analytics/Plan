/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.analysis;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.utilities.TestInit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class AnalysisUtilsTest {

    @Before
    public void setUp() throws Exception {
        TestInit.init();
    }

    @Test
    public void testGetNewPlayers() {
        List<Long> registered = Arrays.asList(5L, 1L);

        long scale = 8L;
        long now = 10L;
        long result = AnalysisUtils.getNewPlayers(registered, scale, now);

        assertEquals(1L, result);
    }

    @Test
    public void testGetNewPlayersEmpty() {
        long scale = 1L;
        long now = 2L;
        long result = AnalysisUtils.getNewPlayers(Collections.emptyList(), scale, now);

        assertEquals(0L, result);
    }
}
