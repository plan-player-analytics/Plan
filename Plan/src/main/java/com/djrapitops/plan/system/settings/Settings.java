package com.djrapitops.plan.system.settings;

import com.djrapitops.plan.system.settings.config.Setting;

/**
 * This enum contains all of the config settings used by the plugin for easier
 * access.
 *
 * @author Rsl1122
 * @since 2.3.2
 */
public enum Settings implements Setting {
    // Boolean
    BUNGEE_COPY_CONFIG("Plugin.Bungee-Override.CopyBungeeConfig"),
    BUNGEE_OVERRIDE_STANDALONE_MODE("Plugin.Bungee-Override.StandaloneMode"),
    ANALYSIS_EXPORT("Analysis.Export.Enabled"),
    ANALYSIS_LOG("Analysis.LogProgress"),
    SHOW_ALTERNATIVE_IP("Commands.AlternativeIP.Enabled"),
    LOG_UNKNOWN_COMMANDS("Data.Commands.LogUnknownCommands"),
    COMBINE_COMMAND_ALIASES("Data.Commands.CombineCommandAliases"),
    WRITE_NEW_LOCALE("Plugin.WriteNewLocaleFileOnEnable"),
    DEV_MODE("Plugin.Dev"),
    USE_SERVER_TIME("Customization.UseServerTime"),
    DISPLAY_SESSIONS_AS_TABLE("Customization.Display.SessionsAsTable"),
    APPEND_WORLD_PERC("Customization.Display.LargestWorldPercInSessionTitle"),
    ORDER_WORLD_PIE_BY_PERC("Customization.Display.OrderWorldPieByPercentage"),
    PLAYERTABLE_FOOTER("Customization.Display.PlayerTableFooter"),
    WEBSERVER_DISABLED("WebServer.DisableWebServer"),
    FORMAT_DATE_RECENT_DAYS("Customization.Formatting.Dates.RecentDays"),
    DISPLAY_PLAYER_IPS("Customization.Display.PlayerIPs"),
    DISPLAY_GAPS_IN_GRAPH_DATA("Customization.Display.GapsInGraphData"),
    DATA_GEOLOCATIONS("Data.Geolocations"),
    NOTIFY_ABOUT_DEV_RELEASES("Plugin.Notify-About-DEV-Releases"),
    CHECK_FOR_UPDATES("Plugin.Check-for-updates"),

    // Integer
    WEBSERVER_PORT("WebServer.Port"),
    DB_PORT("Database.MySQL.Port"),
    ANALYSIS_AUTO_REFRESH("Analysis.AutoRefreshPeriod"),
    ACTIVE_PLAY_THRESHOLD("Analysis.Active.PlaytimeThreshold"),
    ACTIVE_LOGIN_THRESHOLD("Analysis.Active.LoginThreshold"),
    MAX_SESSIONS("Customization.Display.MaxSessions"),
    MAX_PLAYERS("Customization.Display.MaxPlayers"),
    MAX_PLAYERS_PLAYERS_PAGE("Customization.Display.MaxPlayersPlayersPage"),
    AFK_THRESHOLD_MINUTES("Data.AFKThresholdMinutes"),
    KEEP_LOGS_DAYS("Plugin.KeepLogsForXDays"),
    KEEP_INACTIVE_PLAYERS_DAYS("Data.KeepInactivePlayerDataForDays"),
    PING_SERVER_ENABLE_DELAY("Data.Ping.ServerEnableDelaySeconds"),
    PING_PLAYER_LOGIN_DELAY("Data.Ping.PlayerLoginDelaySeconds"),

    // String
    DEBUG("Plugin.Debug"),
    ALTERNATIVE_IP("Commands.AlternativeIP.Link"),
    DB_TYPE("Database.Type"),
    DB_HOST("Database.MySQL.Host"),
    DB_USER("Database.MySQL.User"),
    DB_PASS("Database.MySQL.Password"),
    DB_DATABASE("Database.MySQL.Database"),
    DB_LAUNCH_OPTIONS("Database.MySQL.LaunchOptions"),
    LOCALE("Plugin.Locale"),
    WEBSERVER_IP("WebServer.InternalIP"),
    ANALYSIS_EXPORT_PATH("Analysis.Export.DestinationFolder"),
    WEBSERVER_CERTIFICATE_PATH("WebServer.Security.SSL-Certificate.KeyStorePath"),
    WEBSERVER_CERTIFICATE_KEYPASS("WebServer.Security.SSL-Certificate.KeyPass"),
    WEBSERVER_CERTIFICATE_STOREPASS("WebServer.Security.SSL-Certificate.StorePass"),
    WEBSERVER_CERTIFICATE_ALIAS("WebServer.Security.SSL-Certificate.Alias"),
    EXTERNAL_WEBSERVER_LINK("WebServer.ExternalWebServerAddress"),
    PLUGIN_BUYCRAFT_SECRET("Plugins.BuyCraft.Secret"),

    SERVER_NAME("Server.ServerName"),

    FORMAT_DATE_FULL("Customization.Formatting.Dates.Full"),
    FORMAT_DATE_NO_SECONDS("Customization.Formatting.Dates.NoSeconds"),
    FORMAT_DATE_CLOCK("Customization.Formatting.Dates.JustClock"),
    FORMAT_DATE_RECENT_DAYS_PATTERN("Customization.Formatting.Dates.RecentDays.DatePattern"),
    FORMAT_YEAR("Customization.Formatting.TimeAmount.Year"),
    FORMAT_YEARS("Customization.Formatting.TimeAmount.Years"),
    FORMAT_MONTH("Customization.Formatting.TimeAmount.Month"),
    FORMAT_MONTHS("Customization.Formatting.TimeAmount.Months"),
    FORMAT_DAY("Customization.Formatting.TimeAmount.Day"),
    FORMAT_DAYS("Customization.Formatting.TimeAmount.Days"),
    FORMAT_HOURS("Customization.Formatting.TimeAmount.Hours"),
    FORMAT_MINUTES("Customization.Formatting.TimeAmount.Minutes"),
    FORMAT_SECONDS("Customization.Formatting.TimeAmount.Seconds"),
    FORMAT_ZERO_SECONDS("Customization.Formatting.TimeAmount.Zero"),
    FORMAT_DECIMALS("Customization.Formatting.DecimalPoints"),

    COLOR_MAIN("Commands.Colors.Main"),
    COLOR_SEC("Commands.Colors.Secondary"),
    COLOR_TER("Commands.Colors.Highlight"),

    THEME_BASE("Theme.Base"),
    THEME_GRAPH_TPS_THRESHOLD_HIGH("Theme.Graphs.TPS.High-Threshold"),
    THEME_GRAPH_TPS_THRESHOLD_MED("Theme.Graphs.TPS.Medium-Threshold"),

    // StringList
    HIDE_FACTIONS("Plugins.Factions.HideFactions"),
    HIDE_TOWNS("Plugins.Towny.HideTowns"),
    // Config section
    WORLD_ALIASES("Customization.WorldAliases"),

    // Bungee
    BUNGEE_IP("Server.IP"),
    BUNGEE_NETWORK_NAME("Network.Name");

    private final String configPath;

    Settings(String path) {
        this.configPath = path;
    }

    @Override
    public String getPath() {
        return configPath;
    }

    /**
     * @return What getPath returns
     * @deprecated Old access method used to return string value in config. Use {@link com.djrapitops.plan.system.settings.config.PlanConfig#getString(Setting)} instead
     */
    @Deprecated
    @Override
    public String toString() {
        return getPath();
    }
}
