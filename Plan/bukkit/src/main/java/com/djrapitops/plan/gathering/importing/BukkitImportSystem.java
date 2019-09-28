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
package com.djrapitops.plan.gathering.importing;

import com.djrapitops.plan.gathering.importing.importers.OfflinePlayerImporter;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * ImportSystem implementation for Bukkit.
 *
 * @author Rsl1122
 */
@Singleton
public class BukkitImportSystem extends ImportSystem {

    private final OfflinePlayerImporter offlinePlayerImporter;

    @Inject
    public BukkitImportSystem(
            OfflinePlayerImporter offlinePlayerImporter
    ) {
        this.offlinePlayerImporter = offlinePlayerImporter;
    }

    @Override
    void registerImporters() {
        registerImporter(offlinePlayerImporter);
    }
}