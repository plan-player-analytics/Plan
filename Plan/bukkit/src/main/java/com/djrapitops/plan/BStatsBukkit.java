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

import com.djrapitops.plugin.api.Check;
import org.bstats.bukkit.Metrics;

public class BStatsBukkit {

    private final Plan plugin;
    private Metrics metrics;

    public BStatsBukkit(Plan plugin) {
        this.plugin = plugin;
    }

    public void registerMetrics() {
        if (metrics == null) {
            metrics = new Metrics(plugin, 1240);
        }
        registerConfigSettingGraphs();
    }

    private void registerConfigSettingGraphs() {
        String serverType = plugin.getServer().getName();
        if ("CraftBukkit".equals(serverType) && Check.isSpigotAvailable()) {
            serverType = "Spigot";
        }
        String databaseType = plugin.getSystem().getDatabaseSystem().getDatabase().getType().getName();

        addStringSettingPie("server_type", serverType);
        addStringSettingPie("database_type", databaseType);
    }

    protected void addStringSettingPie(String id, String setting) {
        metrics.addCustomChart(new Metrics.SimplePie(id, () -> setting));
    }
}
