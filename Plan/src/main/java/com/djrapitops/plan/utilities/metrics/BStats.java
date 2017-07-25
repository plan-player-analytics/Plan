package main.java.com.djrapitops.plan.utilities.metrics;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import org.bstats.bukkit.Metrics;

import java.util.concurrent.Callable;

public class BStats {
    private final Plan plugin;
    private Metrics bStats;

    public BStats(Plan plugin) {
        this.plugin = plugin;
    }

    public void registerMetrics() {
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
        boolean gatherChat = Settings.GATHERCHAT.isTrue();
        boolean gatherKills = Settings.GATHERKILLS.isTrue();
        boolean gatherGMTimes = Settings.GATHERGMTIMES.isTrue();
        boolean gatherCommands = Settings.GATHERCOMMANDS.isTrue();

        addEnabledDisabledPie("webserver_enabled", webserver);
        addEnabledDisabledPie("analysis_enable_refresh", analysisRefreshEnable);
        addEnabledDisabledPie("analysis_auto_refresh", analysisAutoRefresh);
        addEnabledDisabledPie("html_export", export);
        addEnabledDisabledPie("gather_chat", gatherChat);
        addEnabledDisabledPie("gather_kills", gatherKills);
        addEnabledDisabledPie("gather_gmtimes", gatherGMTimes);
        addEnabledDisabledPie("gather_commands", gatherCommands);

        String databaseType = Settings.DB_TYPE.toString();
        addStringSettingPie("database_type", databaseType);
    }

    private void addEnabledDisabledPie(String id, boolean setting) {
        bStats.addCustomChart(new Metrics.SimplePie(id, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return setting ? "Enabled" : "Disabled";
            }
        }));
    }

    private void addStringSettingPie(String id, String setting) {
        bStats.addCustomChart(new Metrics.SimplePie(id, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return setting;
            }
        }));
    }
}
