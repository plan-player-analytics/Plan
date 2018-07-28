/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.html;

import org.junit.Test;
import utilities.RandomData;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
public class HtmlUtilsTest {

    @Test
    public void testRemoveXSS() {
        String randomString = RandomData.randomString(10);

        String xss = "<script>" + randomString + "</script><!---->";
        String result = HtmlUtils.removeXSS(xss);

        assertEquals(randomString, result);
    }
}
