/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.ui;

import com.djrapitops.plan.utilities.html.Html;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Rsl1122
 */
public class HtmlTest {

    @Test
    public void testParseWithZeroArgs() {
        String expResult = "${0}</span>";
        String result = Html.SPAN.parse();

        assertEquals(expResult, result);
    }

    @Test
    public void testParseStringArr() {
        String expResult = "Test</span>";
        String result = Html.SPAN.parse("Test");

        assertEquals(expResult, result);
    }

    @Test
    public void testNoBackSlash() {
        assertNotNull(Html.TABLELINE_2.parse("/\\", "0"));
    }
}
