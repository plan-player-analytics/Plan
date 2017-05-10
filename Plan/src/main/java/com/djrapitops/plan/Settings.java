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
    DEBUG("Settings.Debug"),
    WEBSERVER_ENABLED("Settings.WebServer.Enabled"),
    ANALYSIS_REFRESH_ON_ENABLE("Settings.Cache.AnalysisCache.RefreshAnalysisCacheOnEnable"),
    ANALYSIS_LOG_TO_CONSOLE("Settings.Analysis.LogProgressOnConsole"),
    ANALYSIS_LOG_FINISHED("Settings.Analysis.NotifyWhenFinished"),
    SHOW_ALTERNATIVE_IP("Settings.WebServer.ShowAlternativeServerIP"),
    USE_ALTERNATIVE_UI("Settings.UseTextUI"),
    GATHERLOCATIONS("Settings.Data.GatherLocations"),
    SECURITY_IP_UUID("Settings.WebServer.Security.DisplayIPsAndUUIDs"),
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
    ALTERNATIVE_IP("Settings.WebServer.AlternativeIP"),
    DB_TYPE("database.type"),
    DEM_TRIGGERS("Customization.DemographicsTriggers.Trigger"),
    DEM_FEMALE("Customization.DemographicsTriggers.Female"),
    DEM_MALE("Customization.DemographicsTriggers.Male"),
    DEM_IGNORE("Customization.DemographicsTriggers.IgnoreWhen"),
    LOCALE("Settings.Locale"),
    WEBSERVER_IP("Settings.WebServer.InternalIP"),
    SECURITY_CODE("Settings.WebServer.Security.AddressSecurityCode"),
    //
    COLOR_MAIN("Customization.Colors.Commands.Main"),
    COLOR_SEC("Customization.Colors.Commands.Secondary"),
    COLOR_TER("Customization.Colors.Commands.Highlight"),
    //
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
    HCOLOR_GENP_M("Customization.Colors.HTML.GenderPie.Male"),
    HCOLOR_GENP_F("Customization.Colors.HTML.GenderPie.Female"),
    HCOLOR_GENP_U("Customization.Colors.HTML.GenderPie.Unknown"),
    // StringList
    HIDE_FACTIONS("Customization.Plugins.Factions.HideFactions"),
    HIDE_TOWNS("Customization.Plugins.Towny.HideTowns");

    private final String configPath;

    private Settings(String path) {
        this.configPath = path;
    }

    /**
     * If the settings is a boolean, this method should be used.
     *
     * @return Boolean value of the config setting, false if not boolean.
     */
    public boolean isTrue() {
        return Plan.getInstance().getConfig().getBoolean(configPath);
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
