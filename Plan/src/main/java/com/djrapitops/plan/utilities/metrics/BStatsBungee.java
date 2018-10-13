package com.djrapitops.plan.utilities.metrics;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import org.bstats.bungeecord.Metrics;

import java.io.Serializable;

public class BStatsBungee {

    private final PlanBungee plugin;
    private final Database database;
    private final ConnectionSystem connectionSystem;

    private Metrics metrics;

    public BStatsBungee(PlanBungee plugin, Database database, ConnectionSystem connectionSystem) {
        this.plugin = plugin;
        this.database = database;
        this.connectionSystem = connectionSystem;
    }

    public void registerMetrics() {
        if (metrics == null) {
            metrics = new Metrics(plugin);
        }
        registerConfigSettingGraphs();
    }

    private void registerConfigSettingGraphs() {
        String serverType = plugin.getProxy().getName();
        String databaseType = database.getName();

        addStringSettingPie("server_type", serverType);
        addStringSettingPie("database_type", databaseType);
        addStringSettingPie("network_servers", connectionSystem.getBukkitServers().size());
    }

    protected void addStringSettingPie(String id, Serializable setting) {
        metrics.addCustomChart(new Metrics.SimplePie(id, setting::toString));
    }
}
