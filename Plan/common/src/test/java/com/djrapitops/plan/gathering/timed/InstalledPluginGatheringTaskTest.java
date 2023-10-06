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
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.domain.PluginMetadata;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.events.StorePluginVersionsTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.TestConstants;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests to ensure that plugin gathering works as intended.
 *
 * @author AuroraLS3
 */
@ExtendWith(MockitoExtension.class)
class InstalledPluginGatheringTaskTest {

    @Mock
    ServerSensor<?> serverSensor;
    @Mock
    ServerInfo serverInfo;
    @Mock
    DBSystem dbSystem;
    @Mock
    Database database;

    InstalledPluginGatheringTask underTest;

    Transaction capturedTransaction;

    @BeforeEach
    void setup() {
        when(database.executeTransaction(any())).then(invocation -> {
            capturedTransaction = invocation.getArgument(0);
            return CompletableFuture.allOf();
        });
        when(dbSystem.getDatabase()).thenReturn(database);
        when(serverInfo.getServerUUID()).thenReturn(TestConstants.SERVER_UUID);
        underTest = new InstalledPluginGatheringTask(serverSensor, serverInfo, dbSystem);
    }

    @Test
    void newPluginsAreIncluded() {
        List<PluginMetadata> previouslyInstalledPlugins = List.of();
        List<PluginMetadata> installedPlugins = List.of(
                new PluginMetadata("Plan", "5.6 build 2121"),
                new PluginMetadata("LittleChef", "1.0.2")
        );
        when(database.query(any())).thenReturn(previouslyInstalledPlugins);
        when(serverSensor.getInstalledPlugins()).thenReturn(installedPlugins);

        underTest.run();

        List<PluginMetadata> changeList = ((StorePluginVersionsTransaction) capturedTransaction).getChangeList();
        assertEquals(installedPlugins, changeList);
    }


    @Test
    void onlyUpdatedPluginsAreIncluded() {
        List<PluginMetadata> previouslyInstalledPlugins = List.of(
                new PluginMetadata("Plan", "5.6 build 2121"),
                new PluginMetadata("LittleChef", "1.0.2")
        );
        List<PluginMetadata> installedPlugins = List.of(
                new PluginMetadata("Plan", "5.6 build 2121"),
                new PluginMetadata("LittleChef", "1.0.3")
        );
        when(database.query(any())).thenReturn(previouslyInstalledPlugins);
        when(serverSensor.getInstalledPlugins()).thenReturn(installedPlugins);

        underTest.run();

        List<PluginMetadata> expected = List.of(
                new PluginMetadata("LittleChef", "1.0.3")
        );
        List<PluginMetadata> changeList = ((StorePluginVersionsTransaction) capturedTransaction).getChangeList();
        assertEquals(expected, changeList);
    }


    @Test
    void removedPluginsAreIncluded() {
        List<PluginMetadata> previouslyInstalledPlugins = List.of(
                new PluginMetadata("Plan", "5.6 build 2121"),
                new PluginMetadata("LittleChef", "1.0.2")
        );
        List<PluginMetadata> installedPlugins = List.of(
                new PluginMetadata("Plan", "5.6 build 2121")
        );
        when(database.query(any())).thenReturn(previouslyInstalledPlugins);
        when(serverSensor.getInstalledPlugins()).thenReturn(installedPlugins);

        underTest.run();

        List<PluginMetadata> expected = List.of(
                new PluginMetadata("LittleChef", null)
        );
        List<PluginMetadata> changeList = ((StorePluginVersionsTransaction) capturedTransaction).getChangeList();
        assertEquals(expected, changeList);
    }
}