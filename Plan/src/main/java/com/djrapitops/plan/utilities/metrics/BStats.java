package main.java.com.djrapitops.plan.utilities.metrics;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;

public class BStats {
    private final Plan plugin;
    private Metrics metrics;

    public BStats(Plan plugin) {
        this.plugin = plugin;
    }

    public void registerMetrics() {
        Log.debug("Enable", "Enabling bStats Metrics.");
        if (metrics == null) {
            metrics = new Metrics(plugin);
        }
        registerConfigSettingGraphs();
    }

    private void registerConfigSettingGraphs() {
        // TODO Write a Module bar graph
        boolean analysisAutoRefresh = Settings.ANALYSIS_AUTO_REFRESH.getNumber() != -1;
        boolean export = Settings.ANALYSIS_EXPORT.isTrue();

        addEnabledDisabledPie("analysis_auto_refresh", analysisAutoRefresh);
        addEnabledDisabledPie("html_export", export);

        String serverType = plugin.getServer().getName();
        String databaseType = plugin.getDB().getName();

        addStringSettingPie("server_type", serverType);
        addStringSettingPie("database_type", databaseType);
        addStringSettingPie("web_protocol", plugin.getUiServer().getProtocol().toUpperCase());
    }

    private void addEnabledDisabledPie(String id, boolean setting) {
        metrics.addCustomChart(new Metrics.SimplePie(id, () -> setting ? "Enabled" : "Disabled"));
    }

    private void addStringSettingPie(String id, String setting) {
        metrics.addCustomChart(new Metrics.SimplePie(id, () -> setting));
    }
}
