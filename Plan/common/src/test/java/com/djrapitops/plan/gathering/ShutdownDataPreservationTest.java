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
package com.djrapitops.plan.gathering;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.gathering.afk.AFKTracker;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import net.playeranalytics.plugin.PlatformAbstractionLayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.RandomData;
import utilities.TestErrorLogger;
import utilities.TestPluginLogger;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShutdownDataPreservationTest {

    private ShutdownDataPreservation underTest;

    @BeforeEach
    void setupPreservation(@TempDir Path temporaryFolder) {
        PluginMockComponent pluginMockComponent = new PluginMockComponent(temporaryFolder);
        PlanSystem system = pluginMockComponent.getPlanSystem();
        PlatformAbstractionLayer abstractionLayer = pluginMockComponent.getAbstractionLayer();

        TestErrorLogger errorLogger = new TestErrorLogger();
        underTest = new ShutdownDataPreservation(
                system.getPlanFiles(),
                system.getLocaleSystem().getLocale(),
                system.getDatabaseSystem(),
                abstractionLayer.getPluginLogger(),
                errorLogger,
                new ServerShutdownSave(system.getLocaleSystem().getLocale(), system.getDatabaseSystem(), new TestPluginLogger(), errorLogger) {
                    @Override
                    protected boolean checkServerShuttingDownStatus() {
                        return false;
                    }

                    @Override
                    public Optional<AFKTracker> getAfkTracker() {
                        return Optional.empty();
                    }
                });
    }

    @Test
    void dataIsSameAfterStorage() {
        List<FinishedSession> expected = RandomData.randomSessions();
        underTest.storeFinishedSessions(expected);

        List<FinishedSession> result = underTest.loadFinishedSessions();
        assertEquals(expected, result);
    }

    @Test
    void dataIsSameAfterStorageWhenNoSessions() {
        List<FinishedSession> expected = Collections.emptyList();
        underTest.storeFinishedSessions(expected);

        List<FinishedSession> result = underTest.loadFinishedSessions();
        assertEquals(expected, result);
    }

}