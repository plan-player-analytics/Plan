package com.djrapitops.plan.system.locale.lang;

/**
 * {@link Lang} implementation for Language that is logged when the plugin enables or disables.
 *
 * @author Rsl1122
 */
public enum PluginLang implements Lang {
    ENABLED("Enable", "Player Analytics Enabled."),
    ENABLED_WEB_SERVER("Enable - WebServer", "Webserver running on PORT ${0} (${1})"),
    ENABLED_DATABASE("Enable - Database", "${0}-database connection established."),

    ENABLE_NOTIFY_EMPTY_IP("Enable - Notify Empty IP", "IP in server.properties is empty & AlternativeIP is not in use. Incorrect links will be given!"),
    ENABLE_NOTIFY_WEB_SERVER_DISABLED("Enable - Notify Webserver disabled", "WebServer was not initialized. (WebServer.DisableWebServer: true)"),
    ENABLE_NOTIFY_GEOLOCATIONS_DISABLED("Enable - Notify Geolocations disabled", "Geolocation gathering is not active. (Data.Geolocations: false)"),
    ENABLE_NOTIFY_ADDRESS_CONFIRMATION("Enable - Notify Address Confirmation", "Make sure that this address points to THIS Bukkit Server: ${0}"),

    ENABLE_FAIL_DB("Enable FAIL - Database", "${0}-Database Connection failed: ${1}"),
    ENABLE_FAIL_WRONG_DB("Enable FAIL - Wrong Database Type", "${0} is not a supported Database"),

    DISABLED("Disable", "Player Analytics Disabled."),
    DISABLED_WEB_SERVER("Disable - WebServer", "Webserver has been disabled.");

    private final String identifier;
    private final String defaultValue;

    PluginLang(String identifier, String defaultValue) {
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