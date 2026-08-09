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
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Lazy;
import dev.vankka.dependencydownload.ApplicationDependencyManager;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLTransientConnectionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MySQLDBTest {

    @Test
    @SuppressWarnings("unchecked")
    void invalidConnectionIsRejectedWithoutRecursiveLookup() throws Exception {
        Lazy<ServerInfo> serverInfo = mock(Lazy.class);
        MySQLDB database = new MySQLDB(
                mock(Locale.class),
                mock(PlanConfig.class),
                mock(PlanFiles.class),
                serverInfo,
                mock(RunnableFactory.class),
                mock(PluginLogger.class),
                mock(ErrorLogger.class),
                mock(ApplicationDependencyManager.class)
        );
        HikariDataSource dataSource = mock(HikariDataSource.class);
        Connection connection = mock(Connection.class);
        database.dataSource = dataSource;
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(false);

        assertThrows(SQLTransientConnectionException.class, database::getConnection);

        verify(dataSource, times(1)).getConnection();
        verify(connection).close();
    }
}
