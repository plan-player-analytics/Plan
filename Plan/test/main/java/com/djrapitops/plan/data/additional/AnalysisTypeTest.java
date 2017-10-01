/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.additional;

import org.junit.Test;
import test.java.utils.TestUtils;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
public class AnalysisTypeTest {

    @Test
    public void testGetModifier() throws NoSuchFieldException, IllegalAccessException {
        for (AnalysisType type : AnalysisType.values()) {
            assertEquals(TestUtils.getStringFieldValue(type, "modifier"), type.getModifier());
        }
    }

    @Test
    public void testGetPlaceholderModifier() throws NoSuchFieldException, IllegalAccessException {
        for (AnalysisType type : AnalysisType.values()) {
            assertEquals(TestUtils.getStringFieldValue(type, "placeholderModifier"), type.getPlaceholderModifier());
        }
    }
}
