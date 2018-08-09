package com.djrapitops.plan.utilities.metrics;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plugin.api.utility.log.Log;
import org.bstats.bungeecord.Metrics;

import java.io.Serializable;

public class BStatsBungee {

    private final PlanBungee plugin;
    private Metrics metrics;

    public BStatsBungee(PlanBungee plugin) {
        this.plugin = plugin;
    }

    public void registerMetrics() {
        Log.logDebug("Enable", "Enabling bStats Metrics.");
        if (metrics == null) {
            metrics = new Metrics(plugin);
        }
        registerConfigSettingGraphs();
    }

    private void registerConfigSettingGraphs() {
        String serverType = plugin.getProxy().getName();
        String databaseType = Database.getActive().getName();

        addStringSettingPie("server_type", serverType);
        addStringSettingPie("database_type", databaseType);
        addStringSettingPie("network_servers", ConnectionSystem.getInstance().getBukkitServers().size());
    }

    protected void addStringSettingPie(String id, Serializable setting) {
        metrics.addCustomChart(new Metrics.SimplePie(id, setting::toString));
    }
}
