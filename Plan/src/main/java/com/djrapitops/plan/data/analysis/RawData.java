package main.java.com.djrapitops.plan.data.analysis;

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

    private final Map<String, String> replaceMap;

    /**
     * Status info for call to analyzeData method.
     */
    protected boolean analysed;

    /**
     * Only used by subclasses.
     */
    public RawData() {
        replaceMap = new HashMap<>();
        analysed = false;
    }

    /**
     * Check if analyseData() has been called.
     *
     * @return true if the method has been called.
     */
    public boolean isAnalysed() {
        return analysed;
    }

    /**
     * Analyses the data added together.
     * <p>
     * Places place-holders to the replace map.
     */
    public void analyseData() {
        analysed = true;
        this.analyse();
    }

    /**
     * Subclasses should analyse the data added together.
     * <p>
     * Place-holders should be added to the replace map.
     */
    protected abstract void analyse();

    /**
     * Adds values from an existing replaceMap.
     *
     * @param values Map that contains place-holders.
     */
    public void addValues(Map<String, String> values) {
        Verify.nullCheck(values);
        replaceMap.putAll(values);
    }

    /**
     * Adds a placeholder to the replaceMap.
     *
     * @param placeholder placeholder, with or without % signs.
     * @param value       Any value the placeholder should be replaced with.
     */
    public void addValue(String placeholder, Serializable value) {
        replaceMap.put(placeholder, value.toString());
    }

    /**
     * Used to get the placeholders and values.
     *
     * @return Map containing the placeholders and values.
     */
    public Map<String, String> getReplaceMap() {
        return replaceMap;
    }

    /**
     * Used to get the value for a placeholder without the placeholder prefix and suffix.
     *
     * @param key placeholder without the prefix and suffix
     * @return Value the placeholder should be replaced with or null.
     */
    public String get(String key) {
        return replaceMap.get(key);
    }
}
