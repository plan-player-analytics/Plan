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
package com.djrapitops.plan.storage.database.transactions;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.ConfigWriter;
import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.sql.tables.SettingsTable;
import org.apache.commons.text.TextStringBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Transaction to store a server's configuration file in the database.
 *
 * @author AuroraLS3
 */
public class StoreConfigTransaction extends Transaction {

    private final ServerUUID serverUUID;
    private final long lastModified;
    private final String configSettings;

    public StoreConfigTransaction(ServerUUID serverUUID, Config config, long lastModified) {
        this.serverUUID = serverUUID;
        this.configSettings = extractConfigSettingLines(config);
        this.lastModified = lastModified;
    }

    private String extractConfigSettingLines(Config config) {
        TextStringBuilder configTextBuilder = new TextStringBuilder();
        List<String> lines = new ConfigWriter().createLines(config);
        configTextBuilder.appendWithSeparators(lines, "\n");
        return configTextBuilder.toString();
    }

    @Override
    protected void performOperations() {
        if (Boolean.TRUE.equals(query(isConfigStored()))) {
            execute(updateConfig());
        } else {
            execute(insertConfig());
        }
    }

    private Query<Boolean> isConfigStored() {
        String sql = SELECT + "COUNT(1) as c" +
                FROM + SettingsTable.TABLE_NAME +
                WHERE + SettingsTable.SERVER_UUID + "=? LIMIT 1" + lockForUpdate();
        return new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());

            }
        };
    }

    private Executable updateConfig() {
        return new ExecStatement(SettingsTable.UPDATE_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, configSettings);
                statement.setLong(2, lastModified);
                statement.setString(3, serverUUID.toString());
                statement.setString(4, configSettings);
            }
        };
    }

    private Executable insertConfig() {
        return new ExecStatement(SettingsTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, lastModified);
                statement.setString(3, configSettings);
            }
        };
    }
}