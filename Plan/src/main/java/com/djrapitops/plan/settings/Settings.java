package main.java.com.djrapitops.plan.settings;

import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.config.Config;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.util.List;

/**
 * This enum contains all of the config settings used by the plugin for easier
 * access.
 *
 * @author Rsl1122
 * @since 2.3.2
 */
public enum Settings {
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
    ORDER_WORLD_PIE_BY_PERC("Customization.Display.OrderWorldPieByPercentage"),

    // Integer
    WEBSERVER_PORT("WebServer.Port"),
    DB_PORT("Database.MySQL.Port"),
    ANALYSIS_AUTO_REFRESH("Analysis.AutoRefreshPeriod"),
    ACTIVE_PLAY_THRESHOLD("Analysis.Active.PlaytimeThreshold"),
    ACTIVE_LOGIN_THRESHOLD("Analysis.Active.LoginThreshold"),
    MAX_SESSIONS("Customization.Display.MaxSessions"),
    MAX_PLAYERS("Customization.Display.MaxPlayers"),
    MAX_PLAYERS_PLAYERS_PAGE("Customization.Display.MaxPlayersPlayersPage"),

    // String
    DEBUG("Plugin.Debug"),
    ALTERNATIVE_IP("Commands.AlternativeIP.Link"),
    DB_TYPE("Database.Type"),
    DB_HOST("Database.MySQL.Host"),
    DB_USER("Database.MySQL.User"),
    DB_PASS("Database.MySQL.Password"),
    DB_DATABASE("Database.MySQL.Database"),
    LOCALE("Plugin.Locale"),
    WEBSERVER_IP("WebServer.InternalIP"),
    ANALYSIS_EXPORT_PATH("Analysis.Export.DestinationFolder"),
    WEBSERVER_CERTIFICATE_PATH("WebServer.Security.SSL-Certificate.KeyStorePath"),
    WEBSERVER_CERTIFICATE_KEYPASS("WebServer.Security.SSL-Certificate.KeyPass"),
    WEBSERVER_CERTIFICATE_STOREPASS("WebServer.Security.SSL-Certificate.StorePass"),
    WEBSERVER_CERTIFICATE_ALIAS("WebServer.Security.SSL-Certificate.Alias"),
    EXTERNAL_WEBSERVER_LINK_PROTOCOL("Analysis.Export.ExternalWebServerLinkProtocol"),
    //
    SERVER_NAME("Server.ServerName"),
    //
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
    //
    COLOR_MAIN("Commands.Colors.Main"),
    COLOR_SEC("Commands.Colors.Secondary"),
    COLOR_TER("Commands.Colors.Highlight"),
    //
    THEME_BASE("Theme.Base"),
    THEME_FONT_STYLESHEET("Theme.Font.FontStyleSheet"),
    THEME_FONT_FAMILY("Theme.Font.FontFamily"),
    THEME_FONT_COLOR_DARK("Theme.Font.Color.Dark"),
    THEME_FONT_COLOR_LIGHT("Theme.Font.Color.Light"),
    THEME_COLOR_MAIN("Theme.Colors.Main"),
    THEME_COLOR_MAIN_DARK("Theme.Colors.Main-Dark"),
    THEME_COLOR_SECONDARY("Theme.Colors.Secondary"),
    THEME_COLOR_SECONDARY_DARK("Theme.Colors.Secondary-Dark"),
    THEME_COLOR_TERTIARY("Theme.Colors.Tertiary"),
    THEME_COLOR_BACKGROUND("Theme.Colors.Background"),
    THEME_COLOR_TABLE_DARK("Theme.Colors.Table-Dark"),
    THEME_COLOR_TABLE_LIGHT("Theme.Colors.Table-Light"),
    THEME_GRAPH_PUNCHCARD("Theme.Graphs.PunchCard"),
    THEME_GRAPH_PLAYERS_ONLINE("Theme.Graphs.PlayersOnline"),
    THEME_GRAPH_TPS_THRESHOLD_HIGH("Theme.Graphs.TPS.High-Threshold"),
    THEME_GRAPH_TPS_THRESHOLD_MED("Theme.Graphs.TPS.Medium-Threshold"),
    THEME_GRAPH_TPS_HIGH("Theme.Graphs.TPS.High"),
    THEME_GRAPH_TPS_MED("Theme.Graphs.TPS.Medium"),
    THEME_GRAPH_TPS_LOW("Theme.Graphs.TPS.Low"),
    THEME_GRAPH_CPU("Theme.Graphs.CPU"),
    THEME_GRAPH_RAM("Theme.Graphs.RAM"),
    THEME_GRAPH_CHUNKS("Theme.Graphs.Chunks"),
    THEME_GRAPH_ENTITIES("Theme.Graphs.Entities"),
    THEME_GRAPH_WORLD_PIE("Theme.Graphs.WorldPie"),
    THEME_GRAPH_GM_PIE("Theme.Graphs.GMDrilldown"),
    THEME_GRAPH_ACTIVITY_PIE("Theme.Graphs.ActivityPie"),
    THEME_GRAPH_SERVER_PREF_PIE("Theme.Graphs.ServerPreferencePie"),
    // StringList
    HIDE_FACTIONS("Plugins.Factions.HideFactions"),
    HIDE_TOWNS("Plugins.Towny.HideTowns"),
    // Config section
    WORLD_ALIASES("Customization.WorldAliases"),
    //
    // Bungee
    BUNGEE_IP("Server.IP"),
    BUNGEE_NETWORK_NAME("Network.Name");

    private static final ServerSpecificSettings serverSpecificSettings = new ServerSpecificSettings();

    private final String configPath;
    private Boolean value;

    Settings(String path) {
        this.configPath = path;
    }

    /**
     * If the settings is a boolean, this method should be used.
     *
     * @return Boolean value of the config setting, false if not boolean.
     */
    public boolean isTrue() {
        if (value != null) {
            return value;
        }
        return getConfig().getBoolean(configPath);
    }

    public boolean isFalse() {
        return !isTrue();
    }

    public void setValue(Boolean value) {
        this.value = value;
    }

    /**
     * If the settings is a String, this method should be used.
     *
     * @return String value of the config setting.
     */
    @Override
    public String toString() {
        return getConfig().getString(configPath);
    }

    /**
     * If the settings is a number, this method should be used.
     *
     * @return Integer value of the config setting
     */
    public int getNumber() {
        return getConfig().getInt(configPath);
    }

    public List<String> getStringList() {
        return getConfig().getStringList(configPath);
    }

    /**
     * Used to get the String path of a the config setting eg.
     * Settings.WebServer.Enabled
     *
     * @return Path of the config setting.
     */
    public String getPath() {
        return configPath;
    }

    private Config getConfig() {
        return MiscUtils.getIPlan().getMainConfig();
    }

    public static ServerSpecificSettings serverSpecific() {
        if (!Check.isBungeeAvailable()) {
            throw new IllegalStateException("Not supposed to call this method on Bukkit");
        }

        return serverSpecificSettings;
    }
}
