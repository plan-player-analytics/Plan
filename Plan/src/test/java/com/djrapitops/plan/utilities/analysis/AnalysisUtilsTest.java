/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.analysis;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
public class AnalysisUtilsTest {

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
