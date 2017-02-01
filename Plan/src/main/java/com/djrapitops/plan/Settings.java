package main.java.com.djrapitops.plan;

import com.djrapitops.plan.Plan;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public enum Settings {
    WEBSERVER_ENABLED(getPlugin(Plan.class).getConfig().getBoolean("Settings.WebServer.Enabled")),
    WEBSERVER_PORT(getPlugin(Plan.class).getConfig().getInt("Settings.WebServer.Port")),
    ANALYSIS_REFRESH_ON_ENABLE(getPlugin(Plan.class).getConfig().getBoolean("Settings.Cache.AnalysisCache.RefreshAnalysisCacheOnEnable")),
    ANALYSIS_LOG_TO_CONSOLE(getPlugin(Plan.class).getConfig().getBoolean("Settings.Analysis.LogProgressOnConsole")),
    ANALYSIS_MINUTES_FOR_ACTIVE(getPlugin(Plan.class).getConfig().getInt("Settings.Analysis.MinutesPlayedUntilConsidiredActive")),
    SHOW_ALTERNATIVE_IP(getPlugin(Plan.class).getConfig().getBoolean("Settings.WebServer.ShowAlternativeServerIP")),
    ALTERNATIVE_IP(getPlugin(Plan.class).getConfig().getString("Settings.WebServer.AlternativeIP")),
    USE_ALTERNATIVE_UI(getPlugin(Plan.class).getConfig().getBoolean("Settings.PlanLite.UseAsAlternativeUI")),
    PLANLITE_ENABLED(getPlugin(Plan.class).getConfig().getBoolean("Settings.PlanLite.Enabled")),
    GATHERLOCATIONS(getPlugin(Plan.class).getConfig().getBoolean("Settings.Data.GatherLocations")),
    DB_TYPE(getPlugin(Plan.class).getConfig().getString("database.type")),
    SAVE_CACHE_MIN(getPlugin(Plan.class).getConfig().getInt("Settings.Cache.DataCache.SaveEveryXMinutes")),
    SAVE_SERVER_MIN(getPlugin(Plan.class).getConfig().getInt("Settings.Cache.DataCache.SaveServerDataEveryXMinutes")),
    CLEAR_INSPECT_CACHE(getPlugin(Plan.class).getConfig().getInt("Settings.Cache.InspectCache.ClearFromInspectCacheAfterXMinutes")),
    CLEAR_CACHE_X_SAVES(getPlugin(Plan.class).getConfig().getInt("Settings.Cache.DataCache.ClearCacheEveryXSaves")),
    DEM_TRIGGERS(getPlugin(Plan.class).getConfig().getString("Customization.DemographicsTriggers.Trigger")),
    DEM_FEMALE(getPlugin(Plan.class).getConfig().getString("Customization.DemographicsTriggers.Female")),
    DEM_MALE(getPlugin(Plan.class).getConfig().getString("Customization.DemographicsTriggers.Male")),
    DEM_IGNORE(getPlugin(Plan.class).getConfig().getString("Customization.DemographicsTriggers.IgnoreWhen")),
    ;

    private final String text;
    private final boolean bool;
    private final int number;

    private Settings(final String text) {
        this.text = text;
        this.bool = false;
        this.number = -1;
    }

    private Settings(final boolean bool) {
        this.bool = bool;
        this.text = "";
        this.number = -1;
    }

    private Settings(final int number) {
        this.bool = false;
        this.text = "";
        this.number = number;
    }

    @Override
    public String toString() {
        return text;
    }

    public boolean isTrue() {
        return this.bool;
    }

    public int getNumber() {
        return number;
    }
}
