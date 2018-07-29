package com.djrapitops.plan.system.locale.lang;

/**
 * {@link Lang} implementation for commonly used .html replacement values.
 *
 * @author Rsl1122
 */
public enum CommonHtmlLang implements Lang {
    PLEASE_WAIT("Please wait..."),

    NAV_INFORMATION("Information"),
    NAV_SESSIONS("Sessions"),
    NAV_OVERVIEW("Overview"),
    NAV_PLUGINS("Plugins"),
    NAV_ONLINE_ACTIVITY("Online Activity"),
    NAV_SEVER_HEALTH("Server Health"),
    NAV_PERFORMANCE("Performance"),
    NAV_PLAYERS("Players"),
    NAV_GEOLOCATIONS("Geolocations"),
    NAV_COMMAND_USAGE("Command Usage"),
    NAV_NETWORK_PLAYERS("Network Players"),

    AVERAGE_PING("Average Ping"),
    BEST_PING("Best Ping"),
    WORST_PING("Worst Ping"),
    PLAYERS_ONLINE_TEXT("Players Online");

    private final String defaultValue;

    CommonHtmlLang(String defaultValue) {
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