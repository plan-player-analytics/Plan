/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.rendering.html;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for different functions of the {@link Html} class.
 *
 * @author AuroraLS3
 */
class HtmlTest {

    @Test
    void parsingWithNoArgsDoesNotReplacePlaceholder() {
        String expResult = "${0}</span>";
        String result = Html.SPAN.create();

        assertEquals(expResult, result);
    }

    @Test
    void parsingWithArgsReplacesPlaceholder() {
        String expResult = "Test</span>";
        String result = Html.SPAN.create("Test");

        assertEquals(expResult, result);
    }

    @Test
    void colorsToSpanResetsColors() {
        String testString = "§fHello, §aPerson§r - How Are you?";
        String expected = Html.COLOR_F.create() + "Hello, " + Html.COLOR_A.create() + "Person</span></span> - How Are you?";
        String result = Html.swapColorCodesToSpan(testString);
        assertEquals(expected, result);
    }

    @Test
    void colorsToSpanSupportsHex() {
        String testString = "§x§0§f§e§e§d§2Hello, §aPerson§r - How Are you?";
        String expected = "<span style=\"color: #0feed2;\">" + "Hello, " + Html.COLOR_A.create() + "Person</span></span> - How Are you?";
        String result = Html.swapColorCodesToSpan(testString);
        assertEquals(expected, result);
    }

    @Test
    void colorsToSpanSwapsEscapedHtmlColors() {
        String testString = "§fHello, §aPerson§r - How Are you?";
        String expected = Html.COLOR_F.create() + "Hello, " + Html.COLOR_A.create() + "Person</span></span> - How Are you?";
        String result = Html.swapColorCodesToSpan(StringEscapeUtils.escapeHtml4(testString));
        assertEquals(expected, result);
    }

    @Test
    void swapColorCodesDoesNotReplaceNonColors() {
        String testString = "1test§2yes";
        String expected = "1test" + Html.COLOR_2.create() + "yes</span>";
        String result = Html.swapColorCodesToSpan(testString);
        assertEquals(expected, result);
    }
}
