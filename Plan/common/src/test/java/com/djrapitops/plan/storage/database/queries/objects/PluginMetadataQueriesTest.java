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
package com.djrapitops.plan.storage.database.queries.objects;

import com.djrapitops.plan.gathering.domain.PluginMetadata;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.transactions.events.StorePluginVersionsTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link  PluginMetadataQueries}.
 *
 * @author AuroraLS3
 */
public interface PluginMetadataQueriesTest extends DatabaseTestPreparer {

    @Test
    @DisplayName("Plugin Metadata is stored")
    default void pluginMetadataIsStored() {
        List<PluginMetadata> changeSet = List.of(
                new PluginMetadata("Plan", "5.6 build 2121"),
                new PluginMetadata("LittleChef", "1.0.2"),
                new PluginMetadata("LittleFX", null)
        );
        db().executeTransaction(new StorePluginVersionsTransaction(System.currentTimeMillis(), serverUUID(), changeSet));

        List<PluginMetadata> expected = List.of(
                new PluginMetadata("Plan", "5.6 build 2121"),
                new PluginMetadata("LittleChef", "1.0.2")
        );
        List<PluginMetadata> result = db().query(PluginMetadataQueries.getInstalledPlugins(serverUUID()));
        assertEquals(expected, result);
    }

}