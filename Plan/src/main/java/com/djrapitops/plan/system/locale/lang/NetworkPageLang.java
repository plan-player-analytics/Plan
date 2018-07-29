package com.djrapitops.plan.system.locale.lang;

/**
 * {@link Lang} implementation for player.html replacement values.
 *
 * @author Rsl1122
 */
public enum NetworkPageLang implements Lang {
    NETWORK("Network"),
    NETWORK_INFORMATION("NETWORK INFORMATION"),
    PLAYERS_TEXT("Players"),
    PLAYERS("PLAYERS"),
    NEW_TEXT("New"),
    HEALTH_ESTIMATE("Health Estimate");

    private final String defaultValue;

    NetworkPageLang(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return "HTML - " + name();
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}