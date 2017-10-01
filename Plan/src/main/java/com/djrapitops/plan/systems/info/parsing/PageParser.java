/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.parsing;

import main.java.com.djrapitops.plan.api.exceptions.ParseException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class that is responsible for parsing data and html together into a String.
 *
 * @author Rsl1122
 */
public abstract class PageParser {

    protected final Map<String, Serializable> placeHolders;

    public PageParser() {
        this.placeHolders = new HashMap<>();
    }

    protected void addValue(String placeholder, Serializable value) {
        placeHolders.put(placeholder, value);
    }

    protected void addValues(Map<String, Serializable> values) {
        placeHolders.putAll(values);
    }

    public abstract String parse() throws ParseException;
}