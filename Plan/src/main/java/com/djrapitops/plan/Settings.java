package main.java.com.djrapitops.plan;

import main.java.com.djrapitops.plan.Plan;
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
    USE_ALTERNATIVE_UI("Settings.PlanLite.UseAsAlternativeUI"),
    PLANLITE_ENABLED("Settings.PlanLite.Enabled"),
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
    SECURITY_CODE("Settings.WebServer.Security.AddressSecurityCode");

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
