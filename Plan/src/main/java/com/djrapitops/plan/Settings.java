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
    WEBSERVER_ENABLED("Settings.WebServer.Enabled"),
    ANALYSIS_REFRESH_ON_ENABLE("Settings.Cache.AnalysisCache.RefreshAnalysisCacheOnEnable"),
    ANALYSIS_LOG_TO_CONSOLE("Settings.Analysis.LogProgressOnConsole"),
    ANALYSIS_LOG_FINISHED("Settings.Analysis.NotifyWhenFinished"),
    ANALYSIS_REMOVE_OUTLIERS("Settings.Analysis.RemoveOutliersFromVisualization"),
    ANALYSIS_EXPORT("Settings.Analysis.Export.Enabled"),
    SHOW_ALTERNATIVE_IP("Settings.WebServer.ShowAlternativeServerIP"),
    USE_ALTERNATIVE_UI("Settings.UseTextUI"),
    GATHERCHAT("Settings.Data.ChatListener"),
    GATHERKILLS("Settings.Data.GatherKillData"),
    GATHERGMTIMES("Settings.Data.GamemodeChangeListener"),
    GATHERCOMMANDS("Settings.Data.GatherCommandUsage"),
    DO_NOT_LOG_UNKNOWN_COMMANDS("Customization.Data.DoNotLogUnknownCommands"),
    COMBINE_COMMAND_ALIASES_TO_MAIN_COMMAND("Customization.Data.CombineCommandAliasesToMainCommand"),
    SECURITY_IP_UUID("Settings.WebServer.Security.DisplayIPsAndUUIDs"),
    GRAPH_PLAYERS_USEMAXPLAYERS_SCALE("Customization.Graphs.PlayersOnlineGraph.UseMaxPlayersAsScale"),
    PLAYERLIST_SHOW_IMAGES("Customization.SmallHeadImagesOnAnalysisPlayerlist"),
    // Integer
    ANALYSIS_MINUTES_FOR_ACTIVE("Settings.Analysis.MinutesPlayedUntilConsidiredActive"),
    SAVE_CACHE_MIN("Settings.Cache.DataCache.SaveEveryXMinutes"),
    CLEAR_INSPECT_CACHE("Settings.Cache.InspectCache.ClearFromInspectCacheAfterXMinutes"),
    CLEAR_CACHE_X_SAVES("Settings.Cache.DataCache.ClearCacheEveryXSaves"),
    WEBSERVER_PORT("Settings.WebServer.Port"),
    ANALYSIS_AUTO_REFRESH("Settings.Cache.AnalysisCache.RefreshEveryXMinutes"),
    PROCESS_GET_LIMIT("Settings.Cache.Processing.GetLimit"),
    PROCESS_SAVE_LIMIT("Settings.Cache.Processing.SaveLimit"),
    PROCESS_CLEAR_LIMIT("Settings.Cache.Processing.ClearLimit"),
    // String
    DEBUG("Settings.Debug"),
    ALTERNATIVE_IP("Settings.WebServer.AlternativeIP"),
    DB_TYPE("database.type"),
    DEM_TRIGGERS("Customization.DemographicsTriggers.Trigger"),
    DEM_FEMALE("Customization.DemographicsTriggers.Female"),
    DEM_MALE("Customization.DemographicsTriggers.Male"),
    DEM_IGNORE("Customization.DemographicsTriggers.IgnoreWhen"),
    LOCALE("Settings.Locale"),
    WEBSERVER_IP("Settings.WebServer.InternalIP"),
    ANALYSIS_EXPORT_PATH("Settings.Analysis.Export.DestinationFolder"),
    WEBSERVER_CERTIFICATE_PATH("Settings.WebServer.Security.Certificate.KeyStorePath"),
    WEBSERVER_CERTIFICATE_KEYPASS("Settings.WebServer.Security.Certificate.KeyPass"),
    WEBSERVER_CERTIFICATE_STOREPASS("Settings.WebServer.Security.Certificate.KeyPass"),
    WEBSERVER_CERTIFICATE_ALIAS("Settings.WebServer.Security.Certificate.Alias"),
    LINK_PROTOCOL("Settings.WebServer.ExternalWebServerLinkProtocol"),
    //
    SERVER_NAME("Customization.ServerName"),
    //
    FORMAT_YEAR("Customization.Formats.TimeAmount.Year"),
    FORMAT_YEARS("Customization.Formats.TimeAmount.Years"),
    FORMAT_DAY("Customization.Formats.TimeAmount.Day"),
    FORMAT_DAYS("Customization.Formats.TimeAmount.Days"),
    FORMAT_HOURS("Customization.Formats.TimeAmount.Hours"),
    FORMAT_MINUTES("Customization.Formats.TimeAmount.Minutes"),
    FORMAT_SECONDS("Customization.Formats.TimeAmount.Seconds"),
    FORMAT_DECIMALS("Customization.Formats.DecimalPoints"),
    //
    COLOR_MAIN("Customization.Colors.Commands.Main"),
    COLOR_SEC("Customization.Colors.Commands.Secondary"),
    COLOR_TER("Customization.Colors.Commands.Highlight"),
    //
    HCOLOR_MAIN("Customization.Colors.HTML.UI.Main"),
    HCOLOR_MAIN_DARK("Customization.Colors.HTML.UI.MainDark"),
    HCOLOR_SEC("Customization.Colors.HTML.UI.Secondary"),
    HCOLOR_TER("Customization.Colors.HTML.UI.Tertiary"),
    HCOLOR_TER_DARK("Customization.Colors.HTML.UI.TertiaryDark"),
    HCOLOR_ACT_ONL("Customization.Colors.HTML.ActivityGraph.OnlinePlayers"),
    HCOLOR_ACT_ONL_FILL("Customization.Colors.HTML.ActivityGraph.OnlinePlayersFill"),
    HCOLOR_ACTP_ACT("Customization.Colors.HTML.ActivityPie.Active"),
    HCOLOR_ACTP_BAN("Customization.Colors.HTML.ActivityPie.Banned"),
    HCOLOR_ACTP_INA("Customization.Colors.HTML.ActivityPie.Inactive"),
    HCOLOR_ACTP_JON("Customization.Colors.HTML.ActivityPie.JoinedOnce"),
    HCOLOR_GMP_0("Customization.Colors.HTML.GamemodePie.Survival"),
    HCOLOR_GMP_1("Customization.Colors.HTML.GamemodePie.Creative"),
    HCOLOR_GMP_2("Customization.Colors.HTML.GamemodePie.Adventure"),
    HCOLOR_GMP_3("Customization.Colors.HTML.GamemodePie.Spectator"),
    // StringList
    HIDE_FACTIONS("Customization.Plugins.Factions.HideFactions"),
    HIDE_TOWNS("Customization.Plugins.Towny.HideTowns");

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
