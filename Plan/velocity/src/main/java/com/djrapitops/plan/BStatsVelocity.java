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
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;

import java.util.function.Supplier;

public class BStatsVelocity {

    private final Database database;

    private final Metrics metrics;

    public BStatsVelocity(Database database, Metrics metrics) {
        this.database = database;
        this.metrics = metrics;
    }

    public void registerMetrics() {
        registerConfigSettingGraphs();
    }

    private void registerConfigSettingGraphs() {
        addStringSettingPie("database_type", () -> database.getType().getName());
        addStringSettingPie("network_servers", () -> String.valueOf(database.query(ServerQueries.fetchPlanServerInformationCollection()).size()));
    }

    protected void addStringSettingPie(String id, Supplier<String> setting) {
        metrics.addCustomChart(new SimplePie(id, setting::get));
    }
}
