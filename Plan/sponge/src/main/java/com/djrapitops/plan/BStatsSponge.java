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

import com.djrapitops.plan.system.storage.database.Database;
import org.bstats.sponge.Metrics2;

import java.io.Serializable;

public class BStatsSponge {

    private final Metrics2 metrics;
    private final Database database;

    public BStatsSponge(Metrics2 metrics, Database database) {
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
        String databaseType = database.getType().getName();

        addStringSettingPie("server_type", serverType);
        addStringSettingPie("database_type", databaseType);
    }

    protected void addStringSettingPie(String id, Serializable setting) {
        metrics.addCustomChart(new Metrics2.SimplePie(id, setting::toString));
    }
}
