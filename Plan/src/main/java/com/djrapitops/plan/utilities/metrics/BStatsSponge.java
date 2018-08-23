package com.djrapitops.plan.utilities.metrics;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plugin.api.utility.log.Log;
import org.bstats.sponge.Metrics;

import java.io.Serializable;

public class BStatsSponge {

    private final Metrics metrics;

    public BStatsSponge(Metrics metrics) {
        this.metrics = metrics;
    }

    public void registerMetrics() {
        Log.logDebug("Enable", "Enabling bStats Metrics.");
        if (metrics != null) {
            registerConfigSettingGraphs();
        } else {
            Log.debug("Metrics not injected properly.");
        }
    }

    private void registerConfigSettingGraphs() {
        String serverType = "Sponge";
        String databaseType = Database.getActive().getName();

        addStringSettingPie("server_type", serverType);
        addStringSettingPie("database_type", databaseType);
        addStringSettingPie("network_servers", ConnectionSystem.getInstance().getBukkitServers().size());
    }

    protected void addStringSettingPie(String id, Serializable setting) {
        metrics.addCustomChart(new Metrics.SimplePie(id, setting::toString));
    }
}
