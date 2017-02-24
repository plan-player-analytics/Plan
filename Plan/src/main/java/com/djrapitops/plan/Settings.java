package main.java.com.djrapitops.plan;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public enum Settings {
    // Boolean
    WEBSERVER_ENABLED("Settings.WebServer.Enabled"),
    ANALYSIS_REFRESH_ON_ENABLE("Settings.Cache.AnalysisCache.RefreshAnalysisCacheOnEnable"),
    ANALYSIS_LOG_TO_CONSOLE("Settings.Analysis.LogProgressOnConsole"),
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
    // String
    ALTERNATIVE_IP("Settings.WebServer.AlternativeIP"),
    DB_TYPE("database.type"),
    DEM_TRIGGERS("Customization.DemographicsTriggers.Trigger"),
    DEM_FEMALE("Customization.DemographicsTriggers.Female"),
    DEM_MALE("Customization.DemographicsTriggers.Male"),
    DEM_IGNORE("Customization.DemographicsTriggers.IgnoreWhen"),
    LOCALE("Settings.Locale"),
    SECURITY_CODE("Settings.WebServer.Security.AddressSecurityCode"),
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
    HCOLOR_GMP_3("Customization.Colors.HTML.GamemodePie.Spectator");  

    private final String configPath;

    private Settings(String path) {
        this.configPath = path;
    }

    /**
     * @return Boolean value of the config setting
     */
    public boolean isTrue() {
        return getPlugin(Plan.class).getConfig().getBoolean(configPath);
    }

    @Override
    public String toString() {
        return getPlugin(Plan.class).getConfig().getString(configPath);
    }

    /**
     * @return Integer value of the config setting
     */
    public int getNumber() {
        return getPlugin(Plan.class).getConfig().getInt(configPath);
    }
    
    public String getPath() {
        return configPath;
    }
}
