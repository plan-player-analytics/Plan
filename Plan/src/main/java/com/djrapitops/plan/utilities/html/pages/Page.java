/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.pages;

import com.djrapitops.plan.api.exceptions.ParseException;

/**
 * Interface for parsing page HTML.
 *
 * @author Rsl1122
 */
public interface Page {

    String toHtml() throws ParseException;
}