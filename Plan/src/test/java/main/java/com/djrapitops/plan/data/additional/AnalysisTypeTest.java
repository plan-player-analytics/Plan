/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.data.additional;

import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Risto
 */
public class AnalysisTypeTest {
    
    /**
     *
     */
    public AnalysisTypeTest() {
    }
    
    /**
     *
     */
    @Test
    public void testGetModifier() {
        assertEquals("Average ", AnalysisType.INT_AVG.getModifier());
    }

    /**
     *
     */
    @Test
    public void testGetPlaceholderModifier() {
        assertEquals("totalInt_", AnalysisType.INT_TOTAL.getPlaceholderModifier());
    }
    
}
