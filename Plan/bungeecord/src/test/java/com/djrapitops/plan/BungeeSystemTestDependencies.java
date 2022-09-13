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

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import utilities.DBPreparer;

public class BungeeSystemTestDependencies implements DBPreparer.Dependencies {

    private final PlanSystem system;

    public BungeeSystemTestDependencies(PlanSystem system) {
        this.system = system;
    }

    @Override
    public PlanConfig config() {
        return system.getConfigSystem().getConfig();
    }

    @Override
    public DBSystem dbSystem() {
        return system.getDatabaseSystem();
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
    }
}
