package com.djrapitops.plan.system.locale.lang;

/**
 * {@link Lang} implementation for single words.
 *
 * @author Rsl1122
 */
public enum GenericLang implements Lang {
    YES("Positive", "Yes"),
    NO("Negative", "No"),
    UNKNOWN("Unknown", "Unknown");

    private final String identifier;
    private final String defaultValue;

    GenericLang(String identifier, String defaultValue) {
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}