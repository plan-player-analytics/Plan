/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.ui;

import main.java.com.djrapitops.plan.ui.html.Html;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Rsl1122
 */
public class HtmlTest {

    /**
     *
     */
    public HtmlTest() {
    }

    /**
     *
     */
    @Test
    public void testParse_0args() {
        Html instance = Html.REPLACE0;
        String expResult = "REPLACE0";
        String result = instance.parse();
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testParse_StringArr() {
        Html instance = Html.REPLACE0;
        String expResult = "Test";
        String result = instance.parse(expResult);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testSetHtml() {
        Html instance = Html.REPLACE0;
        String expResult = "Test";
        instance.setHtml(expResult);
        String result = instance.parse();
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testNoBackSlash() {
        assertTrue("Null for some reason", Html.TABLELINE_2.parse("/\\", "0") != null);
    }
}
