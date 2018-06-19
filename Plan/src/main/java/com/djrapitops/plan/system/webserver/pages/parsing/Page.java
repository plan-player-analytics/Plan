/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.pages.parsing;

import com.djrapitops.plan.api.exceptions.ParseException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class that is responsible for parsing data and html together into a String.
 *
 * @author Rsl1122
 */
public abstract class Page {

    protected final Map<String, Serializable> placeHolders;

    public Page() {
        this.placeHolders = new HashMap<>();
    }

    protected void addValue(String placeholder, Serializable value) {
        placeHolders.put(placeholder, value);
    }

    protected void addValues(Map<String, Serializable> values) {
        placeHolders.putAll(values);
    }

    public abstract String toHtml() throws ParseException;
}
