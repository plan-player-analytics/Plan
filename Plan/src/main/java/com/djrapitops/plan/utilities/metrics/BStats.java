package main.java.com.djrapitops.plan.utilities.metrics;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;

public class BStats {
    private final Plan plugin;
    private Metrics bStats;

    public BStats(Plan plugin) {
        this.plugin = plugin;
    }

    public void registerMetrics() {
        Log.debug("Enabling bStats Metrics.");
        if (bStats == null) {
            bStats = new Metrics(plugin);
        }
        registerConfigSettingGraphs();
    }

    private void registerConfigSettingGraphs() {
        boolean webserver = Settings.WEBSERVER_ENABLED.isTrue();
        boolean analysisRefreshEnable = Settings.ANALYSIS_REFRESH_ON_ENABLE.isTrue();
        boolean analysisAutoRefresh = Settings.ANALYSIS_AUTO_REFRESH.getNumber() != -1;
        boolean export = Settings.ANALYSIS_EXPORT.isTrue();

        addEnabledDisabledPie("webserver_enabled", webserver);
        addEnabledDisabledPie("analysis_enable_refresh", analysisRefreshEnable);
        addEnabledDisabledPie("analysis_auto_refresh", analysisAutoRefresh);
        addEnabledDisabledPie("html_export", export);

        String serverType = plugin.getServer().getName();
        String databaseType = plugin.getDB().getName();

        addStringSettingPie("server_type", serverType);
        addStringSettingPie("database_type", databaseType);
        addStringSettingPie("web_protocol", plugin.getUiServer().getProtocol().toUpperCase());
    }

    private void addEnabledDisabledPie(String id, boolean setting) {
        bStats.addCustomChart(new Metrics.SimplePie(id, () -> setting ? "Enabled" : "Disabled"));
    }

    private void addStringSettingPie(String id, String setting) {
        bStats.addCustomChart(new Metrics.SimplePie(id, () -> setting));
    }
}
