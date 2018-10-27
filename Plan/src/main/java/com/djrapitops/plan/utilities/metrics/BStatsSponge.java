package com.djrapitops.plan.utilities.metrics;

import com.djrapitops.plan.system.database.databases.Database;
import org.bstats.sponge.Metrics;

import java.io.Serializable;

public class BStatsSponge {

    private final Metrics metrics;
    private final Database database;

    public BStatsSponge(Metrics metrics, Database database) {
        this.metrics = metrics;
        this.database = database;
    }

    public void registerMetrics() {
        if (metrics != null) {
            registerConfigSettingGraphs();
        }
    }

    private void registerConfigSettingGraphs() {
        String serverType = "Sponge";
        String databaseType = database.getName();

        addStringSettingPie("server_type", serverType);
        addStringSettingPie("database_type", databaseType);
    }

    protected void addStringSettingPie(String id, Serializable setting) {
        metrics.addCustomChart(new Metrics.SimplePie(id, setting::toString));
    }
}
