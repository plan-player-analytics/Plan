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
package com.djrapitops.plan.identification;

import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.properties.ServerProperties;
import com.djrapitops.plan.identification.storage.ServerDBLoader;
import com.djrapitops.plan.identification.storage.ServerFileLoader;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import net.playeranalytics.plugin.server.PluginLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.TestConstants;
import utilities.TestErrorLogger;
import utilities.TestPluginLogger;
import utilities.mocks.TestProcessing;

import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author AuroraLS3
 */
@ExtendWith(MockitoExtension.class)
class ServerServerInfoTest {

    @Mock
    DBSystem dbSystem;
    @InjectMocks
    @Spy
    ServerDBLoader fromDatabase;

    @Mock
    ServerProperties serverProperties;
    @Mock
    ServerFileLoader fromFile;
    @Mock
    PlanConfig config;
    @Mock
    Addresses addresses;
    @Mock
    Locale locale;
    @Spy
    PluginLogger logger = new TestPluginLogger();
    @Spy
    Processing processing = new TestProcessing(() -> locale, logger, new TestErrorLogger());

    ServerServerInfo underTest;

    @BeforeEach
    void setUp() {
        TestErrorLogger.throwErrors(false);
        underTest = new ServerServerInfo(
                TestConstants.VERSION,
                serverProperties, fromFile, fromDatabase, processing, config, addresses, locale, logger
        );
        when(fromFile.load(any())).thenReturn(Optional.of(new Server(1, TestConstants.SERVER_UUID, TestConstants.SERVER_NAME, "", false, TestConstants.VERSION)));
    }

    @AfterEach
    void tearDown() {
        for (Throwable throwable : TestErrorLogger.getCaught()) {
            Logger.getGlobal().log(Level.WARNING, "test", throwable);
        }

        TestErrorLogger.throwErrors(true);
    }

    @Test
    void databaseThrowsErrorOnSave() {
        doThrow(new CompletionException(new DBOpException("Test exception")))
                .when(fromDatabase).save(any());
        underTest.loadServerInfo();
        verify(fromFile, times(0)).save(any());
    }
}