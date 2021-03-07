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

import com.djrapitops.plan.storage.database.Database;
import org.bstats.charts.SimplePie;
import org.bstats.sponge.Metrics;

import java.io.Serializable;

public class BStatsSponge implements Runnable {

    private final Metrics metrics;
    private final Database database;

    public BStatsSponge(Metrics metrics, Database database) {
        this.metrics = metrics;
        this.database = database;
    }

    @Override
    public void run() {
        registerMetrics();
    }

    public void registerMetrics() {
        if (metrics != null) {
            registerConfigSettingGraphs();
        }
    }

    private void registerConfigSettingGraphs() {
        String serverType = "Sponge";
        String databaseType = database.getType().getName();

        addStringSettingPie("server_type", serverType);
        addStringSettingPie("database_type", databaseType);
    }

    protected void addStringSettingPie(String id, Serializable setting) {
        metrics.addCustomChart(new SimplePie(id, setting::toString));
    }
}
