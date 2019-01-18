/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan;

import com.djrapitops.plan.db.Database;
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
        String databaseType = database.getType().getName();

        addStringSettingPie("server_type", serverType);
        addStringSettingPie("database_type", databaseType);
        addStringSettingPie("network_servers", connectionSystem.getBukkitServers().size());
    }

    protected void addStringSettingPie(String id, Serializable setting) {
        metrics.addCustomChart(new Metrics.SimplePie(id, setting::toString));
    }
}
