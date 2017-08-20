package main.java.com.djrapitops.plan;

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
    BUNGEE_COPY_CONFIG("Bungee-Override.CopyBungeeConfig"),
    BUNGEE_OVERRIDE_STANDALONE_MODE("Bungee-Override.StandaloneMode"),
    @Deprecated WEBSERVER_ENABLED("Settings.WebServer.Enabled"),
    @Deprecated ANALYSIS_REFRESH_ON_ENABLE("Settings.Cache.AnalysisCache.RefreshAnalysisCacheOnEnable"),
    @Deprecated ANALYSIS_LOG_TO_CONSOLE("Settings.Analysis.LogProgressOnConsole"),
    @Deprecated ANALYSIS_LOG_FINISHED("Settings.Analysis.NotifyWhenFinished"),
    ANALYSIS_EXPORT("Analysis.Export.Enabled"),
    SHOW_ALTERNATIVE_IP("Commands.AlternativeIP.Enabled"),
    @Deprecated USE_ALTERNATIVE_UI("Settings.UseTextUI"),
    LOG_UNKNOWN_COMMANDS("DAta.Commands.LogUnknownCommands"),
    COMBINE_COMMAND_ALIASES("Data.Commands.CombineCommandAliases"),
    @Deprecated SECURITY_IP_UUID("Settings.WebServer.Security.DisplayIPsAndUUIDs"),
    @Deprecated PLAYERLIST_SHOW_IMAGES("Customization.SmallHeadImagesOnAnalysisPlayerlist"),
    WRITE_NEW_LOCALE("Plugin.WriteNewLocaleFileOnStart"),

    // Integer
    @Deprecated ANALYSIS_MINUTES_FOR_ACTIVE("Settings.Analysis.MinutesPlayedUntilConsidiredActive"),
    @Deprecated SAVE_CACHE_MIN("Settings.Cache.DataCache.SaveEveryXMinutes"),
    @Deprecated CLEAR_INSPECT_CACHE("Settings.Cache.InspectCache.ClearFromInspectCacheAfterXMinutes"),
    @Deprecated CLEAR_CACHE_X_SAVES("Settings.Cache.DataCache.ClearCacheEveryXSaves"),
    WEBSERVER_PORT("WebServer.Port"),
    ANALYSIS_AUTO_REFRESH("Analysis.AutoRefreshPeriod"),
    @Deprecated PROCESS_GET_LIMIT("Settings.Cache.Processing.GetLimit"),
    @Deprecated PROCESS_SAVE_LIMIT("Settings.Cache.Processing.SaveLimit"),
    @Deprecated PROCESS_CLEAR_LIMIT("Settings.Cache.Processing.ClearLimit"),
    @Deprecated TPS_GRAPH_HIGH("Customization.Colors.HTML.TPSGraph.TPSHigh"),
    @Deprecated TPS_GRAPH_MED("Customization.Colors.HTML.TPSGraph.TPSMedium"),
    // String
    DEBUG("Plugin.Debug"),
    ALTERNATIVE_IP("Commands.AlternativeIP.Link"),
    DB_TYPE("Database.Type"),
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
    FORMAT_DECIMALS("Customization.Formatting.DecimalPoints"),
    //
    COLOR_MAIN("Commands.Colors.Main"),
    COLOR_SEC("Commands.Colors.Secondary"),
    COLOR_TER("Commands.Colors.Highlight"),
    //
    THEME_BASE("Theme.Base"),
    THEME_FONT_STYLESHEET("Theme.Font.FontStyleSheet"),
    THEME_FONT_FAMILY("Theme.Font.FontFamily"),
    THEME_FONT_COLOR_DARK("Theme.Font.Dark"),
    THEME_FONT_COLOR_LIGHT("Theme.Font.Light"),
    THEME_COLOR_MAIN("Theme.Colors.Main"),
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
    //
    @Deprecated HCOLOR_MAIN("Customization.Colors.HTML.UI.Main"),
    @Deprecated HCOLOR_MAIN_DARK("Customization.Colors.HTML.UI.MainDark"),
    @Deprecated HCOLOR_SEC("Customization.Colors.HTML.UI.Secondary"),
    @Deprecated HCOLOR_TER("Customization.Colors.HTML.UI.Tertiary"),
    @Deprecated HCOLOR_TER_DARK("Customization.Colors.HTML.UI.TertiaryDark"),
    @Deprecated HCOLOR_TPS_HIGH("Customization.Colors.HTML.TPSGraph.TPSHighCol"),
    @Deprecated HCOLOR_TPS_MED("Customization.Colors.HTML.TPSGraph.TPSMediumCol"),
    @Deprecated HCOLOR_TPS_LOW("Customization.Colors.HTML.TPSGraph.TPSLowCol"),
    @Deprecated HCOLOR_ACT_ONL("Customization.Colors.HTML.ActivityGraph.OnlinePlayers"),
    @Deprecated HCOLOR_ACTP_ACT("Customization.Colors.HTML.ActivityPie.Active"),
    @Deprecated HCOLOR_ACTP_BAN("Customization.Colors.HTML.ActivityPie.Banned"),
    @Deprecated HCOLOR_ACTP_INA("Customization.Colors.HTML.ActivityPie.Inactive"),
    @Deprecated HCOLOR_ACTP_JON("Customization.Colors.HTML.ActivityPie.JoinedOnce"),
    @Deprecated HCOLOR_GMP_0("Customization.Colors.HTML.GamemodePie.Survival"),
    @Deprecated HCOLOR_GMP_1("Customization.Colors.HTML.GamemodePie.Creative"),
    @Deprecated HCOLOR_GMP_2("Customization.Colors.HTML.GamemodePie.Adventure"),
    @Deprecated HCOLOR_GMP_3("Customization.Colors.HTML.GamemodePie.Spectator"),
    // StringList
    HIDE_FACTIONS("Plugins.Factions.HideFactions"),
    HIDE_TOWNS("Plugins.Towny.HideTowns");

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
        return Plan.getInstance().getConfig().getBoolean(configPath);
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
        return Plan.getInstance().getConfig().getString(configPath);
    }

    /**
     * If the settings is a number, this method should be used.
     *
     * @return Integer value of the config setting
     */
    public int getNumber() {
        return Plan.getInstance().getConfig().getInt(configPath);
    }

    public List<String> getStringList() {
        return Plan.getInstance().getConfig().getStringList(configPath);
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
}
