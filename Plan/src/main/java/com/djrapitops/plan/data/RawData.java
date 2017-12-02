package main.java.com.djrapitops.plan.data;

import com.djrapitops.plugin.utilities.Verify;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Extending objects should represent, add together and analyse data.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public abstract class RawData {

    private final Map<String, Serializable> replaceMap;

    /**
     * Only used by subclasses.
     */
    public RawData() {
        replaceMap = new HashMap<>();
    }

    /**
     * Adds values from an existing replaceMap.
     *
     * @param values Map that contains place-holders.
     */
    public void addValues(Map<String, Serializable> values) {
        Verify.nullCheck(values);
        replaceMap.putAll(values);
    }

    /**
     * Adds a placeholder to the replaceMap.
     *
     * @param placeholder placeholder, without prefix and suffix
     * @param value       Any value the placeholder should be replaced with.
     */
    public void addValue(String placeholder, Serializable value) {
        replaceMap.put(placeholder, value);
    }

    /**
     * Used to get the placeholders and values.
     *
     * @return Map containing the placeholders and values.
     */
    public Map<String, Serializable> getReplaceMap() {
        return replaceMap;
    }

    /**
     * Used to get the value for a placeholder without the placeholder prefix and suffix.
     *
     * @param key placeholder name without ${ and }
     * @return Value the placeholder should be replaced with or null.
     */
    public Serializable get(String key) {
        return replaceMap.get(key);
    }
}
